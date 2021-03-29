package net.forthecrown.pirates;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.exceptions.CannotAffordTransaction;
import net.forthecrown.core.exceptions.CrownException;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PirateEvents implements Listener {

    private final Pirates main;

    public PirateEvents(Pirates main){
        this.main = main;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEntityEvent event) {
        if(!event.getHand().equals(EquipmentSlot.HAND))
            return;

        Player player = event.getPlayer();
        CrownUser user = FtcCore.getUser(player.getUniqueId());
        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            if (event.getRightClicked().getName().contains(ChatColor.GOLD + "Wilhelm"))
            {
                event.setCancelled(true);

                if (main.getConfig().getStringList("PlayerWhoSoldHeadAlready").contains(player.getUniqueId().toString()))
                {
                    player.sendMessage(ChatColor.GRAY + "You've already sold a " + main.getConfig().getString("ChosenHead") + ChatColor.GRAY + " head today.");
                    return;
                }
                if (main.checkIfInvContainsHead(event.getPlayer().getInventory())) {
                    //player.getWorld().playEffect(event.getRightClicked().getLocation(), Effect.VILLAGER_PLANT_GROW, 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                    main.giveReward(player);
                    List<String> temp = main.getConfig().getStringList("PlayerWhoSoldHeadAlready");
                    temp.add(player.getUniqueId().toString());
                    main.getConfig().set("PlayerWhoSoldHeadAlready", temp);
                    main.saveConfig();
                }
                else {
                    player.sendMessage(ChatColor.GOLD + "{FTC} " + ChatColor.RESET + "Bring Wilhelm a " + main.getConfig().getString("ChosenHead") + ChatColor.RESET + " head for a reward.");
                }
            } else if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Jack")) {
                event.setCancelled(true);
                main.grapplingHook.openLevelSelector(player);
            } else if (event.getRightClicked().getName().contains(ChatColor.YELLOW + "Ben")){
                if(!user.getAvailableRanks().contains(Rank.PIRATE)) throw new CrownException(user, "&eBen &7only trusts real pirates!");
                if(FtcCore.getBalances().get(player.getUniqueId()) < 50000) throw new CannotAffordTransaction(player);

                FtcCore.getBalances().add(player.getUniqueId(), -50000);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gh give " + player.getName() + " 50");
                user.sendMessage("&7You bought a grappling hook from &eBen &7for &e50,000 Rhines");
            }

        }
        else if (event.getRightClicked().getType() == EntityType.SHULKER) {
            Shulker treasureShulker = (Shulker) event.getRightClicked();
            if ((!treasureShulker.hasAI()) && treasureShulker.getColor() == DyeColor.GRAY) {
                if (main.getConfig().getStringList("PlayerWhoFoundTreasureAlready").contains(player.getUniqueId().toString())) player.sendMessage(ChatColor.GRAY + "You've already opened this treasure today.");
                else {
                    main.giveTreasure(player);
                    List<String> temp = main.getConfig().getStringList("PlayerWhoFoundTreasureAlready");
                    temp.add(player.getUniqueId().toString());
                    main.getConfig().set("PlayerWhoFoundTreasureAlready", temp);
                    main.saveConfig();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (event.getHand().equals(EquipmentSlot.HAND)) {
                if (event.getItem() != null && event.getItem().getType() == Material.COMPASS) {
                    if (event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(main.getConfig().getString("TreasureLoc.world"))) {

                        Location targetLoc = new Location(Bukkit.getWorld(main.getConfig().getString("TreasureLoc.world")),
                                main.getConfig().getInt("TreasureLoc.x"),
                                main.getConfig().getInt("TreasureLoc.y"),
                                main.getConfig().getInt("TreasureLoc.z"));

                        event.getPlayer().setCompassTarget(targetLoc);
                        Location playerloc = event.getPlayer().getLocation();
                        playerloc.getWorld().playSound(playerloc, Sound.ITEM_LODESTONE_COMPASS_LOCK, 1, 1);
                        //playerloc.getWorld().spawnParticle(Particle.END_ROD, playerloc.getX(), playerloc.getY()+0.5, playerloc.getZ(), 5, 0.7, 0, 0.7, 0.02);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(main.offlineWithParrots);
        List<String> players = yaml.getStringList("Players");
        if (players.contains(event.getPlayer().getUniqueId().toString())) {
            event.getPlayer().setShoulderEntityLeft(null);
            players.remove(event.getPlayer().getUniqueId().toString());
            yaml.set("Players", players);
            main.saveyaml(yaml, main.offlineWithParrots);
        }
    }

    // parrot uuid - player uuid
    public Map<UUID, UUID> parrots = new HashMap<>();

    @EventHandler
    public void onParrotDismount(CreatureSpawnEvent event) {
        if (parrots.containsKey(event.getEntity().getUniqueId())) {

            if (Bukkit.getPlayer(parrots.get(event.getEntity().getUniqueId())).isFlying()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, () -> {
                    event.getEntity().remove();
                    Bukkit.getPlayer(parrots.get(event.getEntity().getUniqueId())).sendMessage(ChatColor.GRAY + "Poof! Parrot gone.");
                    parrots.remove(event.getEntity().getUniqueId());
                }, 1L);
            }
            else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onParrotDeath(EntityDeathEvent event) {
        if (parrots.containsKey(event.getEntity().getUniqueId())) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            parrots.remove(event.getEntity().getUniqueId());
        }
    }
}
