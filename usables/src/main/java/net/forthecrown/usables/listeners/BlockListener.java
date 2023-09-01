package net.forthecrown.usables.listeners;

import net.forthecrown.usables.Usables;
import net.forthecrown.usables.objects.UsableBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BlockListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    Block block = event.getClickedBlock();
    Player player = event.getPlayer();

    assert block != null;

    if (!Usables.isUsable(block)) {
      return;
    }

    UsableBlock usable = Usables.block(block);
    usable.load();

    UsablesListeners.executeInteract(usable, player, event);
  }


}
