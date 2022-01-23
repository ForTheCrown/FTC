package net.forthecrown.events;

import net.forthecrown.core.Crown;
import net.forthecrown.useables.UsablesManager;
import net.forthecrown.utils.Cooldown;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class UsablesListeners implements Listener {
    private final UsablesManager manager = Crown.getUsables();
    private static final String cooldownCategory = "Core_Interactables";

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        boolean b = check(event.getClickedBlock(), event.getPlayer());
        if(b && !event.isCancelled()) event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        boolean b = check(event.getRightClicked(), event.getPlayer(), event.getHand());
        if(b && !event.isCancelled()) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(manager.isInteractableEntity(event.getEntity())){
            try {
                manager.getEntity(event.getEntity()).delete();
            } catch (NullPointerException ignored) {}
        }
    }

    public boolean check(Entity entity, Player player, EquipmentSlot slot){
        if(slot != EquipmentSlot.HAND) return false;
        if(check0(player)) return false;

        try {
            if(manager.isInteractableEntity(entity)) {
                manager.getEntity(entity).interact(player);
                return true;
            }
        } catch (NullPointerException ignored) {}

        return false;
    }

    private boolean check0(Player player) {
        if(player.getGameMode() == GameMode.SPECTATOR) return true;
        return Cooldown.containsOrAdd(player, cooldownCategory, 10);
    }

    // Returns whether the event should be cancelled
    public boolean check(Block block, Player player){
        if(check0(player)) return false;

        try {
            if(manager.isInteractableSign(block)) {
                manager.getBlock(block.getLocation()).interact(player);
                return true;
            }
        } catch (NullPointerException ignored) {}

        return false;
    }
}
