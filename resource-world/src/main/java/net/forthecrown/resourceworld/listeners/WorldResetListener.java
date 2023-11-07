package net.forthecrown.resourceworld.listeners;

import static net.forthecrown.resourceworld.Constants.BUKKIT_HEIGHT_MAP;
import static net.forthecrown.resourceworld.Constants.WG_OVERREACH;
import static net.forthecrown.resourceworld.Constants.WG_SIZE_Y;

import com.google.common.base.Strings;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.time.Duration;
import net.forthecrown.Loggers;
import net.forthecrown.McConstants;
import net.forthecrown.resourceworld.RwPlugin;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.structure.Structures;
import net.forthecrown.text.channel.ChannelledMessage;
import net.forthecrown.usables.CmdUsables;
import net.forthecrown.usables.UsablesPlugin;
import net.forthecrown.usables.conditions.TestCooldown;
import net.forthecrown.usables.conditions.TestWorld;
import net.forthecrown.usables.objects.Warp;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.worldloader.WorldLoadCompleteEvent;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Orientable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mcteam.ancientgates.Gate;
import org.slf4j.Logger;
import org.spongepowered.math.vector.Vector3i;

public class WorldResetListener implements Listener {

  public static final Logger LOGGER = Loggers.getLogger();

  private final RwPlugin plugin;

  public WorldResetListener(RwPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onWorldLoadComplete(WorldLoadCompleteEvent event) {
    onWorldLoaded(event.getWorld());
  }

  private void onWorldLoaded(World world) {
    var config = plugin.getRwConfig();

    var spawnOptional = Structures.get()
        .getRegistry()
        .get(config.spawnStructure);

    if (spawnOptional.isEmpty()) {
      LOGGER.error("Cannot find RW spawn, no structure with key {} found", config.spawnStructure);
      return;
    }

    BlockStructure spawn = spawnOptional.get();

    int y = world.getHighestBlockYAt(0, 0, BUKKIT_HEIGHT_MAP) + 1;

    // Take the size of the spawn structure, divide in half
    // and make the vector negative, set the y to first empty
    // Y block, we've now got a place position for the spawn
    Vector3i placePos = spawn.getPalette(null).getSize()
        .div(-2)
        .withY(y);

    StructurePlaceConfig placeConfig = StructurePlaceConfig.builder()
        .addNonNullProcessor()
        .addRotationProcessor()
        .world(world)
        .pos(placePos)
        .build();

    // Place spawn structure
    spawn.place(placeConfig);
    LOGGER.info("Placed rw spawn at {}", placeConfig.getDestination());

    Vector3i minUnder = placePos.withY(y - 1);
    Vector3i maxUnder = placePos.mul(-1).withY(y - (WG_SIZE_Y / 2)).sub(1, 0, 1);

    // Make sure there are no air blocks under
    // the spawn
    WorldBounds3i underArea = WorldBounds3i.of(world, minUnder, maxUnder);
    Orientable data = (Orientable) Material.STRIPPED_DARK_OAK_WOOD.createBlockData();
    data.setAxis(Axis.X);

    for (Block b : underArea) {
      // Don't replace solid blocks
      if (b.isSolid()) {
        continue;
      }

      b.setBlockData(data, false);
    }

    // Move gates' locations
    Location gateLocation = new Location(world, -6, y + 1, 0, -90, 0);
    Location destination = new Location(world, 0, y + 2, 0);

    world.setSpawnLocation(destination);

    Gate toHaz = Gate.get(config.toHazGate);
    Gate toRes = Gate.get(config.toResGate);

    // Move Haz -> RW gate from position
    if (toHaz == null) {
      LOGGER.warn(
          "Could not re-position rw -> haz gate, could not find gate with id {}",
          config.toHazGate
      );
    } else {
      // First null forces the 'from' list to be
      // emptied, second method call actually sets
      // the 'from' location. Don't ask me why it's
      // like this, this was in /setfrom's code
      toHaz.addFrom(null);
      toHaz.addFrom(gateLocation);

      LOGGER.info("Moved rw -> haz gate to {}", Vectors.intFrom(gateLocation));
    }

    // Move RW -> Haz gate destination
    if (toRes == null) {
      LOGGER.warn(
          "Could not re-position haz -> rw gate, could not find gate with id {}",
          config.toResGate
      );
    } else {
      // Same deal with the 2 method calls as above
      toRes.addTo(null);
      toRes.addTo(destination);

      LOGGER.info("Moved haz -> rw destination to {}", Vectors.intFrom(destination));
    }

    // Move portal warp or create it
    if (PluginUtil.isEnabled("FTC-Usables")) {
      CmdUsables<Warp> warps = UsablesPlugin.get().getWarps();
      String warpName = config.portalWarp;

      Warp portalWarp = warps.get(warpName);

      if (portalWarp == null) {
        // Check doesn't exist, create it
        portalWarp = warps.getFactory().create(warpName);
        portalWarp.setDestination(destination);

        // Make sure it has the in_world check and cooldown
        var checks = portalWarp.getConditions();
        checks.addFirst(new TestCooldown(Duration.ofMinutes(5)));
        checks.addFirst(new TestWorld(world));

        warps.add(portalWarp);
      } else {
        portalWarp.setDestination(destination);
      }
    }

    // Redefine WorldGuard region to match spawn bounds
    if (!Strings.isNullOrEmpty(config.worldGuardSpawn)) {
      // Figure out bounds
      BlockVector3 min = BlockVector3.at(
          placePos.x() - WG_OVERREACH,
          y - WG_SIZE_Y,
          placePos.z() - WG_OVERREACH
      );

      BlockVector3 max = min.abs().withY(McConstants.MAX_Y);

      // Get manager and region
      RegionManager manager = WorldGuard.getInstance()
          .getPlatform()
          .getRegionContainer()
          .get(BukkitAdapter.adapt(world));

      ProtectedRegion region = manager.getRegion(config.worldGuardSpawn);

      // Region might be null, idk how that might happen but still, just in case
      if (region == null) {
        LOGGER.info("Couldn't find world guard spawn region, creating...");

        region = new ProtectedCuboidRegion(config.worldGuardSpawn, min, max);
      } else {
        LOGGER.info("Moving world guard spawn region to match new RW spawn");

        // This is apparently how region redefining is done, you remove
        // the region, create a new one and copy everything from the last
        // one and put the new region back into the manager
        manager.removeRegion(region.getId(), RemovalStrategy.REMOVE_CHILDREN);
        ProtectedRegion old = region;

        region = new ProtectedCuboidRegion(config.worldGuardSpawn, min, max);
        region.copyFrom(old);
      }

      manager.addRegion(region);
    } else {
      LOGGER.warn("wgSpawnName in resourceWorld is null, cannot edit spawn region");
    }

    // Attempt to announce the end's reset being finished
    if (config.messages.resetEnd == null) {
      LOGGER.warn("resetEnd message is null, cannot announce opening");
    } else {
      ChannelledMessage.announce(config.messages.resetEnd);
    }

    var rw = plugin.getResourceWorld();

    rw.setGatesOpen(true);

    rw.lastReset = System.currentTimeMillis();
    LOGGER.info("Resource World reset finished");
  }
}
