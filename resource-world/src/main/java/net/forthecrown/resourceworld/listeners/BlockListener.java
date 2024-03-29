package net.forthecrown.resourceworld.listeners;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.forthecrown.Worlds;
import net.forthecrown.resourceworld.RwPlugin;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.math.Vectors;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class BlockListener implements Listener {

  private final Random random;
  private final RwPlugin plugin;

  public BlockListener(RwPlugin plugin) {
    this.plugin = plugin;
    this.random = new Random();
  }

  private boolean testWorld(World world) {
    if (!plugin.getRwConfig().doubleDrop.enabled) {
      return false;
    }

    return world.equals(Worlds.resource());
  }

  private boolean testBlock(Block block) {
    return plugin.getTracker().isNatural(Vectors.from(block));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onBlockBreakEvent(BlockBreakEvent event) {
    // Cancel double dropping if:
    // Not in the RW
    if (!testWorld(event.getBlock().getWorld())
        // If broken block is a chest or something, it'd
        // be too OP if it worked on containers
        || event.getBlock().getState() instanceof Container

        // If they were unlucky
        || plugin.getRwConfig().doubleDrop.rate < random.nextFloat(1f)

        // Not a natural block or in creative
        || !testBlock(event.getBlock())
        || event.getPlayer().getGameMode() == GameMode.CREATIVE
    ) {
      return;
    }

    // Since we can't modify the drops directly in the
    // event, we cancel all drops
    event.setDropItems(false);

    Player player = event.getPlayer();
    ItemStack breaking = player.getInventory().getItemInMainHand();

    // Process drops, aka double everything that's dropped
    Collection<ItemStack> drops = event.getBlock().getDrops(breaking, player);
    Collection<ItemStack> toDrop = processDrops(drops);

    // Drop all new drops
    World world = event.getBlock().getWorld();
    Location l = event.getBlock().getLocation();

    for (ItemStack i : toDrop) {
      world.dropItemNaturally(l, i.clone());
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onBlockBreak(BlockBreakEvent event) {
    onBlockBecomeNonNatural(event);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onBlockPlace(BlockPlaceEvent event) {
    onBlockBecomeNonNatural(event);
  }

  private void onBlockBecomeNonNatural(BlockEvent event) {
    if (!testWorld(event.getBlock().getWorld())) {
      return;
    }

    plugin.getTracker().setNonNatural(Vectors.from(event.getBlock()));
  }

  private Collection<ItemStack> processDrops(Collection<ItemStack> drops) {
    if (drops == null || drops.isEmpty()) {
      return drops;
    }

    List<ItemStack> toDrop = new ObjectArrayList<>();

    // Process drops
    for (ItemStack i : drops) {
      if (ItemStacks.isEmpty(i)) {
        continue;
      }

      // Just add 2 of the item to the result
      // ClearLagg will merge the items anyway
      // if they can be merged
      toDrop.add(i.clone());
    }

    return toDrop;
  }
}