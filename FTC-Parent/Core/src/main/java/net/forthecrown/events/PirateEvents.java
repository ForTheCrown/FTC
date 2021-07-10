package net.forthecrown.events;

import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.CrownCore;
import net.forthecrown.economy.pirates.PirateEconomy;
import net.forthecrown.economy.pirates.merchants.UsablePirateNpc;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.pirates.TreasureShulker;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.utils.Cooldown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.spigotmc.event.entity.EntityDismountEvent;

public class PirateEvents implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event1) {
        if(event1.getRightClicked().getPersistentDataContainer().has(Pirates.BM_MERCHANT, PersistentDataType.STRING)){
            if(!(event1.getRightClicked() instanceof Villager)) return;

            Entity entity = event1.getRightClicked();

            String id = entity.getPersistentDataContainer().get(Pirates.BM_MERCHANT, PersistentDataType.STRING);
            assert  id != null : "Id was null";

            event1.setCancelled(true);

            CrownUser user = UserManager.getUser(event1.getPlayer());
            PirateEconomy bm = Pirates.getPirateEconomy();

            if(user.getBranch() != Branch.PIRATES){
                user.sendMessage(Component.translatable("pirates.wrongBranch", NamedTextColor.YELLOW, entity.customName()));
                return;
            }

            UsablePirateNpc npc = bm.getNpcById(id);
            npc.onUse(user, entity);
            return;
        }

        if(event1.getRightClicked().getPersistentDataContainer().has(Pirates.SHULKER_KEY, PersistentDataType.BYTE)){
            Events.handlePlayer(event1, event -> {
                Player player = event.getPlayer();
                if(Cooldown.contains(player)) return;
                Cooldown.add(player, 16);

                CrownUser user = UserManager.getUser(player);
                if(user.getBranch() != Branch.PIRATES) throw FtcExceptionProvider.notPirate();

                TreasureShulker shulker = Pirates.getTreasure();
                if(shulker.createLoot(player, event.getRightClicked()).giveRewards(player)){
                    shulker.find(user.getUniqueId());
                    shulker.relocate();
                }
            });
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (!event.getHand().equals(EquipmentSlot.HAND)) return;
        if (event.getItem() == null || event.getItem().getType() != Material.COMPASS) return;
        if (!event.getPlayer().getWorld().equals(CrownCore.getTreasureWorld())) return;
        if (UserManager.getUser(event.getPlayer()).getBranch() != Branch.PIRATES) return;

        event.getPlayer().setCompassTarget(Pirates.getTreasure().getLocation());
        Location playerloc = event.getPlayer().getLocation();
        playerloc.getWorld().playSound(playerloc, Sound.ITEM_LODESTONE_COMPASS_LOCK, 1, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDismount(EntityDismountEvent event) {
        if(!(event.getEntity() instanceof Parrot)) return;
        if(!(event.getDismounted() instanceof Player)) return;
        if(!Pirates.getParrotTracker().contains(event.getDismounted().getUniqueId())) return;

        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY) return;
        if(!(event.getEntity() instanceof Parrot)) return;
        if(!Pirates.getParrotTracker().contains(((Parrot) event.getEntity()).getOwnerUniqueId())) return;

        event.setCancelled(true);
    }
}
