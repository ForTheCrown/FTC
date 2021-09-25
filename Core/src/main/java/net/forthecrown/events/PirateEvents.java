package net.forthecrown.events;

import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ComVars;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.pirates.TreasureShulker;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.user.data.Faction;
import net.forthecrown.utils.Cooldown;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
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

    //Treasure shulker find thing
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event1) {
        if (!event1.getRightClicked().getPersistentDataContainer().has(Pirates.SHULKER_KEY, PersistentDataType.BYTE)) return;

        Events.handlePlayer(event1, event -> {
            Player player = event.getPlayer();
            if(Cooldown.containsOrAdd(player, 16)) return;

            CrownUser user = UserManager.getUser(player);
            if(user.getFaction() != Faction.PIRATES) throw FtcExceptionProvider.notPirate();

            TreasureShulker shulker = Pirates.getTreasure();

            //if the loot was successfully given, relocate shulker and add player to loot found list
            if(shulker.createLoot(player, event.getRightClicked()).giveRewards(player)) {
                shulker.find(user.getUniqueId());
                shulker.relocate();
            }
        });
    }

    //Right click in RW for compass to find treasure
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (!event.getHand().equals(EquipmentSlot.HAND)) return;
        if (event.getItem() == null || event.getItem().getType() != Material.COMPASS) return;
        Player player = event.getPlayer();

        if (!player.getWorld().equals(ComVars.getTreasureWorld())) return;
        if (UserManager.getUser(player).getFaction() != Faction.PIRATES) return;

        player.setCompassTarget(Pirates.getTreasure().getLocation());
        Location playerloc = player.getLocation();
        playerloc.getWorld().playSound(playerloc, Sound.ITEM_LODESTONE_COMPASS_LOCK, 1, 1);
    }

    //Parrot dismount thing
    @EventHandler(ignoreCancelled = true)
    public void onEntityDismount(EntityDismountEvent event) {
        if(!(event.getEntity() instanceof Parrot)) return;
        if(!(event.getDismounted() instanceof Player)) return;
        if(!Pirates.getParrotTracker().contains(event.getDismounted().getUniqueId())) return;

        event.setCancelled(true);
    }

    //Parrot dismount thing
    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY) return;
        if(!(event.getEntity() instanceof Parrot)) return;
        if(!Pirates.getParrotTracker().contains(((Parrot) event.getEntity()).getOwnerUniqueId())) return;

        event.setCancelled(true);
    }
}
