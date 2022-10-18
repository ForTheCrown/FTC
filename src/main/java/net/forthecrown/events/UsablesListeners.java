package net.forthecrown.events;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.forthecrown.datafix.UsablesJsonToTag;
import net.forthecrown.useables.UsableBlock;
import net.forthecrown.useables.UsableEntity;
import net.forthecrown.useables.Usables;
import net.forthecrown.utils.Cooldown;
import net.forthecrown.utils.EntityIdentifier;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class UsablesListeners implements Listener {
    private static final String COOLDOWN_CATEGORY = "Core_Interactables";

    @EventHandler(ignoreCancelled = true)
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        if (!Usables.get().isUsableEntity(event.getEntity())) {
            return;
        }

        var manager = Usables.get();
        var entity = event.getEntity();
        var usable = manager.getLoadedEntity(entity);

        if (usable == null) {
            usable = new UsableEntity(entity.getUniqueId());
            usable.load(entity.getPersistentDataContainer());
        } else {
            manager.entityUnload(usable);
        }

        usable.setIdentifier(EntityIdentifier.of(entity));
        usable.save(entity.getPersistentDataContainer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityAddToWorld(EntityAddToWorldEvent event) {
        var entity = event.getEntity();
        var container = entity.getPersistentDataContainer();

        if (!container.has(Usables.USABLE_KEY) && !container.has(Usables.LEGACY_KEY)) {
            return;
        }

        UsablesJsonToTag.convertLegacy(entity);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityMove(EntityMoveEvent event) {
        if (!Usables.get().isUsableEntity(event.getEntity())) {
            return;
        }

        var manager = Usables.get();
        var entity = event.getEntity();
        var usable = manager.getEntity(entity);

        var fChunk = event.getFrom().getChunk();
        var tChunk = event.getTo().getChunk();

        if (fChunk.getX() != tChunk.getX() || fChunk.getZ() != tChunk.getZ()) {
            usable.setIdentifier(
                    EntityIdentifier.of(entity)
            );

            usable.save(entity.getPersistentDataContainer());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        boolean b = check(event.getClickedBlock(), event.getPlayer());

        if (b && !event.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        boolean b = check(event.getRightClicked(), event.getPlayer(), event.getHand());

        if (b && !event.isCancelled()) {
            event.setCancelled(true);
        }
    }

    public boolean check(Entity entity, Player player, EquipmentSlot slot){
        if (slot != EquipmentSlot.HAND) {
            return false;
        }

        var manager = Usables.get();

        try {
            if (manager.isUsableEntity(entity)) {
                if (check0(player)) {
                    return false;
                }

                UsableEntity usable = manager.getEntity(entity);
                usable.interact(player);
                usable.save(entity.getPersistentDataContainer());

                return usable.isCancelVanilla();
            }
        } catch (NullPointerException ignored) {}

        return false;
    }

    private boolean check0(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }

        return Cooldown.containsOrAdd(player, COOLDOWN_CATEGORY, 10);
    }

    // Returns whether the event should be cancelled
    public boolean check(Block block, Player player) {
        var manager = Usables.get();

        try {
            if (manager.isUsableBlock(block)) {
                if (check0(player)) {
                    return false;
                }

                UsableBlock usable = manager.getBlock(block);
                usable.interact(player);
                usable.save();

                return usable.isCancelVanilla();
            }
        } catch (NullPointerException ignored) {}

        return false;
    }
}