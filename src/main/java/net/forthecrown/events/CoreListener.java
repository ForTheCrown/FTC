package net.forthecrown.events;

import net.forthecrown.core.Messages;
import net.forthecrown.core.config.GeneralConfig;
import net.forthecrown.core.npc.Npcs;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.utils.text.ChatParser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class CoreListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent event) {
    if (event.getBlock().getType() != Material.HOPPER
        || GeneralConfig.hoppersInOneChunk == -1
    ) {
      return;
    }

    int hopperAmount = event.getBlock()
        .getChunk()
        .getTileEntities(block -> block.getType() == Material.HOPPER, true)
        .size();

    if (hopperAmount <= GeneralConfig.hoppersInOneChunk) {
      return;
    }

    event.setCancelled(true);
    event.getPlayer().sendMessage(Messages.tooManyHoppers());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void onSignChange(SignChangeEvent event) {
    Player player = event.getPlayer();

    // Disable case checking on signs
    ChatParser parser = ChatParser.of(player)
        .addFlags(ChatParser.FLAG_IGNORE_CASE);

    for (int i = 0; i < 4; i++) {
      event.line(i, parser.render(event.getLine(i)));
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDropItem(PlayerDropItemEvent event) {
    if (ExtendedItems.isSpecial(event.getItemDrop().getItemStack())) {
      event.getPlayer().sendActionBar(
          Messages.CANNOT_DROP_SPECIAL
      );

      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (!event.getRightClicked()
        .getPersistentDataContainer()
        .has(Npcs.KEY, PersistentDataType.STRING)
    ) {
      return;
    }

    event.setCancelled(true);
    event.setCancelled(
        Npcs.interact(
            event.getRightClicked().getPersistentDataContainer()
                .get(Npcs.KEY, PersistentDataType.STRING),
            event.getRightClicked(),
            event.getPlayer(),
            event.isCancelled()
        )
    );
  }
}