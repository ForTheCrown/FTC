package net.forthecrown.emperor.events;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.useables.UsablesManager;
import net.forthecrown.emperor.utils.Cooldown;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class InteractableEvents implements Listener {
    private final UsablesManager manager = CrownCore.getUsablesManager();
    private static final String cooldownCategory = "Core_Interactables";

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        check(event.getClickedBlock(), event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        check(event.getRightClicked(), event.getPlayer(), event.getHand());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        check(event.getRightClicked(), event.getPlayer(), event.getHand());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        if(manager.isInteractableEntity(event.getEntity())){
            try {
                manager.getEntity(event.getEntity()).delete();
            } catch (NullPointerException ignored) {}
        }
    }

    public void check(Entity entity, Player player, EquipmentSlot slot){
        if(slot != EquipmentSlot.HAND) return;
        if(player.getGameMode() == GameMode.SPECTATOR) return;

        if(Cooldown.contains(player, cooldownCategory)) return;
        Cooldown.add(player, cooldownCategory, 10);

        try {
            if(manager.isInteractableEntity(entity)) manager.getEntity(entity).interact(player);
        } catch (NullPointerException ignored) {}
    }

    public void check(Block block, Player player){
        if(player.getGameMode() == GameMode.SPECTATOR) return;

        if(Cooldown.contains(player, cooldownCategory)) return;
        Cooldown.add(player, cooldownCategory, 10);

        try {
            if(manager.isInteractableSign(block)) manager.getSign(block.getLocation()).interact(player);
        } catch (NullPointerException ignored) {}
    }
}
