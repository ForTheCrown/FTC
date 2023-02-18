package net.forthecrown.events;

import net.forthecrown.core.logging.Loggers;
import net.forthecrown.useables.UsableBlock;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.Usables;
import net.forthecrown.utils.Cooldown;
import org.apache.logging.log4j.Logger;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class UsablesListeners implements Listener {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final String COOLDOWN_CATEGORY = "Core_Interactables";

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    boolean cancelVanilla = interactBlock(
        event.getClickedBlock(),
        event.getPlayer()
    );

    if (cancelVanilla) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    boolean cancelVanilla = interactEntity(
        event.getRightClicked(),
        event.getPlayer(),
        event.getHand()
    );

    if (cancelVanilla) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
    if (!(event.getRightClicked() instanceof ArmorStand)) {
      return;
    }

    // Same logic as this listener, but different handler list, so gotta
    // have this method
    onPlayerInteractEntity(event);
  }

  public boolean interactEntity(Entity entity, Player player, EquipmentSlot slot) {
    if (slot != EquipmentSlot.HAND) {
      return false;
    }

    var manager = Usables.getInstance();

    try {
      if (!manager.isUsableEntity(entity)) {
        return false;
      }

      UsableEntity usable = manager.getEntity(entity);

      if (shouldRunInteraction(player)) {
        usable.interact(player);
        usable.save(entity.getPersistentDataContainer());
      }

      return usable.cancelVanilla();
    } catch (NullPointerException e) {
      LOGGER.error(
          "Error running entity interaction between player {} and {}",
          player, entity, e
      );
    }

    return false;
  }

  private boolean shouldRunInteraction(Player player) {
    if (player.getGameMode() == GameMode.SPECTATOR) {
      return false;
    }

    return !Cooldown.containsOrAdd(player, COOLDOWN_CATEGORY, 10);
  }

  // Returns whether the event should be cancelled
  public boolean interactBlock(Block block, Player player) {
    var manager = Usables.getInstance();

    try {
      if (!manager.isUsableBlock(block)) {
        return false;
      }

      UsableBlock usable = manager.getBlock(block);

      if (shouldRunInteraction(player)) {
        usable.interact(player);
        usable.save();
      }

      return usable.cancelVanilla();
    } catch (NullPointerException e) {
      LOGGER.error("Error running block interaction at {}, player: {}",
          block, player, e
      );
    }

    return false;
  }
}