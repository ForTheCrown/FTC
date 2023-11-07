package net.forthecrown.core.listeners;

import net.forthecrown.core.CorePlugin;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class HopperListener implements Listener {

  private final CorePlugin plugin;

  public HopperListener(CorePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    int max = plugin.getFtcConfig().hoppersInOneChunk();

    if (event.getBlock().getType() != Material.HOPPER || max == -1) {
      return;
    }

    int hopperAmount = event.getBlock()
        .getChunk()
        .getTileEntities(block -> block.getType() == Material.HOPPER, true)
        .size();

    if (hopperAmount <= max) {
      return;
    }

    event.setCancelled(true);
    event.getPlayer().sendMessage(
        Text.format("Too many hoppers in one chunk! &7(Max {0, number})", NamedTextColor.GRAY, max)
    );
  }
}
