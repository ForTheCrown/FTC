package net.forthecrown.core;

import static net.forthecrown.core.config.EndConfig.closeMessage;
import static net.forthecrown.core.config.EndConfig.enabled;
import static net.forthecrown.core.config.EndConfig.leverPos;
import static net.forthecrown.core.config.EndConfig.nextSize;
import static net.forthecrown.core.config.EndConfig.open;
import static net.forthecrown.core.config.EndConfig.openMessage;
import static net.forthecrown.core.logging.Loggers.STAFF_LOG;
import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.google.gson.JsonArray;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.forthecrown.core.config.EndConfig;
import net.forthecrown.core.config.JoinInfo;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.world.WorldLoader;
import net.forthecrown.utils.world.WorldReCreator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Switch;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;

/**
 * The end opener should automatically close and open the end portal in Hazelguard. It should be
 * kept open for the last 7 days of each week, aka, end week.
 */
public class EndOpener {

  private static final Logger LOGGER = Loggers.getLogger();
  private static final EndOpener INSTANCE = new EndOpener();

  private EndOpener() {
    PortalRoad.saveDefaults();
  }

  public static EndOpener get() {
    return INSTANCE;
  }

  @OnDayChange
  void onDayChange(ZonedDateTime time) {
    // If we've disabled this, don't run
    if (!enabled) {
      return;
    }

    byte day = (byte) time.getDayOfMonth();
    byte first = 1;
    byte last = (byte) time.toLocalDate().lengthOfMonth();
    byte openingDay = (byte) (last - 7);

    // The range representing days on which the
    // portal should be closed
    Range<Byte> closedRange = Range.between(first, openingDay);

    // If it's the middle of the month, reset the end
    // Preferably perform the resetting asynchronously.
    int rangeDif = closedRange.getMaximum() - closedRange.getMinimum();
    int resetDay = closedRange.getMinimum() + (rangeDif / 2);

    if (day == resetDay) {
      regenerateEnd();
    }

    // If current day is a closed day, but portal not closed,
    // close it, if not close day and portal not open, open it
    // This is messy, but IDK how to do it better
    if (closedRange.contains(day)) {
      if (open) {
        setOpen(false);
      }
    } else if (!open) {
      setOpen(true);
    }
  }

  /**
   * Regenerates the end with a new seed.
   *
   * @return A completable future which is completed when {@link WorldLoader} finished loading the
   * new end world.
   */
  public CompletableFuture<World> regenerateEnd() {
    LOGGER.info(STAFF_LOG, "Starting End reset");

    // Re-create world
    WorldReCreator reCreator = WorldReCreator.of(Worlds.end())
        .preserveGameRules(true)
        .preserveSeed(false)
        .preserveWorldBorder(true);

    final World created = reCreator.run();

    // Sometimes the border size is incorrect
    // so gotta call this with our manually
    // set size
    created.getWorldBorder().setSize(nextSize);

    // Load the world and create the crucial
    // End Features needed for the end to
    // function properly
    return WorldLoader.loadAsync(created)
        .whenComplete((world, throwable) -> {
          // Run sync
          Tasks.runSync(() -> {
            try {
              EndDragonFight fight = VanillaAccess.getLevel(world).getDragonFight();

              // Create exit portal
              world.getEnderDragonBattle().generateEndPortal(true);

              // Place gateways
              new EndGateWayPlacer().place(fight);
            } catch (ReflectiveOperationException e) {
              e.printStackTrace();
            }

            LOGGER.info(STAFF_LOG, "End reset finished");
          });
        });
  }

  /**
   * Opens or closes the end
   *
   * @param open Whether the end is to be open
   */
  public void setOpen(boolean open) {
    // If lever setting fails, don't proceed
    if (!setLever(open)) {
      return;
    }

    EndConfig.open = open;
    JoinInfo.endInfo.visible = open;

    PortalRoad.set(leverPos.getWorld(), open);
    Announcer.get().announce(open ? openMessage : closeMessage);

    var world = Worlds.end();
    var border = world.getWorldBorder();

    if (open) {
      border.setSize(nextSize);
    } else {
      border.setSize(1);
    }

    String openClosed = open ? "open" : "closed";

    LOGGER.info(STAFF_LOG, "End is now {}", openClosed);
    DiscordBotAnnouncer.announce("The End is now %s", openClosed);
  }

  // Lever on = closed, lever off = open. AKA, flip the input
  // Returns: True, if successfully changed lever, false otherwise
  private boolean setLever(boolean open) {
    boolean on = !open;

    Block b = leverPos.getBlock();

    if (b.getType() != Material.LEVER) {
      LOGGER.error(
          "Given EndOpener lever position: {} is not a lever! Cannot "
              + "close/open end",
          leverPos
      );
      return false;
    }

    Switch data = (Switch) b.getBlockData();
    data.setPowered(on);

    b.setBlockData(data, true);

    // Update redstone
    // Thank you once more, Bukkit, for not providing
    // an API for this stuff. Cuz who could ever want
    // to update a block state
    Level world = VanillaAccess.getLevel(b.getWorld());
    CraftBlock craft = (CraftBlock) b;
    BlockPos pos = craft.getPosition();
    BlockState state = craft.getNMS();

    world.updateNeighborsAt(pos, Blocks.LEVER);
    world.updateNeighborsAt(pos.relative(getConnectedDirection(state).getOpposite()), Blocks.LEVER);
    return true;
  }

  // Copy and pasted method from LeverBlock class, it was protected there
  // So I had to lol
  protected static Direction getConnectedDirection(BlockState state) {
    return switch (state.getValue(LeverBlock.FACE)) {
      case CEILING -> Direction.DOWN;
      case FLOOR -> Direction.UP;
      default -> state.getValue(LeverBlock.FACING);
    };
  }

  static class PortalRoad {

    static void saveDefaults() {
      Path filePath = PathUtil.pluginPath("end_opener.json");

      try {
        FtcJar.saveResources(
            "end_opener.json",
            filePath,
            ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
        );
      } catch (IOException exc) {
        LOGGER.error("Error saving end_opener.json to disk", exc);
        return;
      }
    }

    public static void set(World world, boolean glass) {
      saveDefaults();

      Path filePath = PathUtil.pluginPath("end_opener.json");
      JsonWrapper json = SerializationHelper.readJson(filePath)
          .map(JsonWrapper::wrap)
          .resultOrPartial(LOGGER::error)
          .orElseThrow();

      List<PortalRoadEntry> entries
          = readEntries(json.getArray("path_positions"));

      List<Material> pathMaterials
          = readMaterials(json.getArray("road_materials"));

      if (entries.isEmpty()) {
        LOGGER.warn("No portal road entries found! Cannot replace path blocks");
        return;
      }

      if (pathMaterials.isEmpty()) {
        LOGGER.warn("No road_materials found, cannot replace");
        return;
      }

      for (PortalRoadEntry e : entries) {
        Block b = world.getBlockAt(e.x, e.y, e.z);

        b.setType(
            glass
                ? Material.PURPLE_STAINED_GLASS
                : e.material(pathMaterials)
        );
      }
    }

    static List<PortalRoadEntry> readEntries(JsonArray array) {
      return JsonUtils.stream(array)
          .map(e -> {
            JsonWrapper json = JsonWrapper.wrap(e.getAsJsonObject());
            int x = json.getInt("x");
            int y = json.getInt("y");
            int z = json.getInt("z");

            String matName = json.getString("material");
            Material material = matName == null
                ? null
                : Material.matchMaterial(matName);

            return new PortalRoadEntry(x, y, z, material);
          })

          .collect(Collectors.toList());
    }

    static List<Material> readMaterials(JsonArray array) {
      return JsonUtils.stream(array)
          .map(element -> Material.getMaterial(element.getAsString()))
          .collect(Collectors.toList());
    }

    record PortalRoadEntry(int x, int y, int z, Material material) {

      public Material material(List<Material> randoms) {
        if (material != null) {
          return material;
        }

        return randoms.get(Util.RANDOM.nextInt(randoms.size()));
      }
    }
  }

  private static class EndGateWayPlacer {

    private final Method newGateway;
    private final Field possibleGateways;

    private EndGateWayPlacer() throws ReflectiveOperationException {
      newGateway = EndDragonFight.class.getDeclaredMethod("n");
      newGateway.setAccessible(true);

      possibleGateways = EndDragonFight.class.getDeclaredField("m");
      possibleGateways.setAccessible(true);
    }

    private void place(EndDragonFight fight) throws ReflectiveOperationException {
      List<Integer> possible = (List<Integer>) possibleGateways.get(fight);
      int length = possible.size();

      for (int i = 0; i < length; i++) {
        newGateway.invoke(fight);
      }
    }
  }
}