package net.forthecrown.pirates;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.CrownException;
import net.forthecrown.commands.clickevent.ClickEventManager;
import net.forthecrown.commands.clickevent.ClickEventTask;
import net.forthecrown.economy.Balances;
import net.forthecrown.economy.CannotAffordTransactionException;
import net.forthecrown.core.events.handler.CrownEventExecutor;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.enums.Branch;
import net.forthecrown.user.enums.Rank;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PirateEvents implements Listener, ClickEventTask {

    private final Pirates main;
    private final String npcID;

    public PirateEvents(Pirates main){
        this.main = main;
        npcID = ClickEventManager.registerClickEvent(this);
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEntityEvent event1) throws CrownException {
        if(!event1.getHand().equals(EquipmentSlot.HAND)) return;

        CrownEventExecutor.handlePlayer(event1, event -> {
            Player player = event.getPlayer();
            CrownUser user = UserManager.getUser(player.getUniqueId());
            if (event.getRightClicked().getType() == EntityType.VILLAGER) {
                if (event.getRightClicked().getName().contains(ChatColor.GOLD + "Wilhelm")) {
                    event.setCancelled(true);

                    if (main.getConfig().getStringList("PlayerWhoSoldHeadAlready").contains(player.getUniqueId().toString())) {
                        player.sendMessage(ChatColor.GRAY + "You've already sold a " + main.getConfig().getString("ChosenHead") + ChatColor.GRAY + " head today.");
                        return;
                    }

                    if (main.checkIfInvContainsHead(event.getPlayer().getInventory())) {
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
                    if(CrownCore.getBalances().get(player.getUniqueId()) < 50000) throw new CannotAffordTransactionException(player);

                    ClickEventManager.allowCommandUsage(player, true);

                    Component text = Component.text("Would you like to ")
                            .color(NamedTextColor.GRAY)
                            .append(
                                    Component.text("[Buy] ")
                                            .color(NamedTextColor.AQUA)
                                            .clickEvent(ClickEventManager.getClickEvent(npcID))
                                            .hoverEvent(HoverEvent.showText(Component.text("Click me!")))
                            )
                            .append(Component.text("a "))
                            .append(Component.text("50 use ").color(NamedTextColor.YELLOW))
                            .append(Component.text("Grappling Hook for "))
                            .append(Balances.formatted(50000).color(NamedTextColor.YELLOW))
                            .append(Component.text("?"));

                    player.sendMessage(text);
                }

            }
            else if (event.getRightClicked().getType() == EntityType.SHULKER) {
                Shulker treasureShulker = (Shulker) event.getRightClicked();

                if (!treasureShulker.getPersistentDataContainer().has(TreasureShulker.KEY, PersistentDataType.BYTE)) return;
                if(user.getBranch() != Branch.PIRATES) throw new CrownException(user, "&eOnly pirates can use this! &6Join the pirates in Questmoor");

                if (main.getConfig().getStringList("PlayerWhoFoundTreasureAlready").contains(player.getUniqueId().toString())) player.sendMessage(ChatColor.GRAY + "You've already opened this treasure today.");
                else {
                    main.giveTreasure(player);
                    List<String> temp = main.getConfig().getStringList("PlayerWhoFoundTreasureAlready");
                    temp.add(player.getUniqueId().toString());
                    main.getConfig().set("PlayerWhoFoundTreasureAlready", temp);
                    main.saveConfig();
                    main.shulker.relocate();
                }
            }
        });
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (!event.getHand().equals(EquipmentSlot.HAND)) return;
        if (event.getItem() == null || event.getItem().getType() != Material.COMPASS) return;
        if (!event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(main.getConfig().getString("TreasureLoc.world"))) return;

        Location targetLoc = new Location(Bukkit.getWorld(main.getConfig().getString("TreasureLoc.world")),
                main.getConfig().getInt("TreasureLoc.x"),
                main.getConfig().getInt("TreasureLoc.y"),
                main.getConfig().getInt("TreasureLoc.z"));

        event.getPlayer().setCompassTarget(targetLoc);
        Location playerloc = event.getPlayer().getLocation();
        playerloc.getWorld().playSound(playerloc, Sound.ITEM_LODESTONE_COMPASS_LOCK, 1, 1);
        //playerloc.getWorld().spawnParticle(Particle.END_ROD, playerloc.getX(), playerloc.getY()+0.5, playerloc.getZ(), 5, 0.7, 0, 0.7, 0.02);
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
            main.saveYaml(yaml, main.offlineWithParrots);
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

    @Override
    public void run(Player player, String[] strings) throws CrownException {
        CrownUser user = UserManager.getUser(player);

        if(!user.getAvailableRanks().contains(Rank.PIRATE)) throw new CrownException(user, "&eBen &7only trusts real pirates!");
        if(CrownCore.getBalances().get(player.getUniqueId()) < 50000) throw new CannotAffordTransactionException(player);

        CrownCore.getBalances().add(player.getUniqueId(), -50000);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gh give " + player.getName() + " 50");
        player.sendMessage(
                Component.text("You bought a grappling hook from ")
                        .color(NamedTextColor.GRAY)
                        .append(Component.text("Ben ").color(NamedTextColor.YELLOW))
                        .append(Component.text("for "))
                        .append(Balances.formatted(50000).color(NamedTextColor.YELLOW))
        );
    }
}
