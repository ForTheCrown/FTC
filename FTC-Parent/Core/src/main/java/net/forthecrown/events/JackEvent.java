package net.forthecrown.events;

import net.forthecrown.core.CrownCore;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.pirates.grappling.GhLevelData;
import net.forthecrown.pirates.grappling.GhLevelSelector;
import net.forthecrown.pirates.grappling.GrapplingHookParkour;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.PlayerInventory;

public class JackEvent implements Listener {
    public final GhLevelSelector selector;

    private final GrapplingHookParkour parkour;
    public JackEvent() {
        this.parkour = Pirates.getParkour();
        this.selector = new GhLevelSelector(parkour);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if(!(event.getRightClicked() instanceof Villager)) return;
        Villager villie = (Villager) event.getRightClicked();

        if(villie.customName() == null) return;
        if(!villie.isCustomNameVisible()) return;
        if(!villie.customName().contains(Component.text("Jack").color(NamedTextColor.YELLOW))) return;

        Player player = event.getPlayer();
        Bukkit.getPluginManager().registerEvents(new JackInvListener(player), CrownCore.inst());
    }

    public class JackInvListener implements Listener {
        private final Player player;

        public JackInvListener(Player player) {
            this.player = player;
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClick(InventoryClickEvent event) {
            if(!event.getWhoClicked().equals(player)) return;
            if(!(event.getView().getTopInventory().getHolder() instanceof GhLevelSelector)) return;
            if(event.isShiftClick()) event.setCancelled(true);
            if(event.getClickedInventory() instanceof PlayerInventory) return;

            event.setCancelled(true);
            if(event.getCurrentItem() == null) return;

            Material type = event.getCurrentItem().getType();
            if(type == Material.BARRIER) {
                player.openInventory(selector.getInventory());
                return;
            } else if(type == Material.GREEN_WOOL){
                CrownUser user = UserManager.getUser(player);
                parkour.getData().removeAllFor(user.getUniqueId());

                user.sendMessage(Component.translatable("gh.progressReset", NamedTextColor.YELLOW));
                return;
            } else if(type == Material.RED_WOOL){
                player.openInventory(selector.create(UserManager.getUser(player)));
                return;
            }

            int index = event.getSlot() - 1;
            Location dest;

            if(index == 39) index = 35;

            if(index == -1){
                dest = new Location(player.getWorld(), -1003.5, 21, 3.5, 180, 0); // Level 1 start
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gh give " + player.getName());
            } else {
                GhLevelData data = parkour.getData().get("Stand_" + index);
                assert data != null : "Data was null";

                dest = data.getExitDest().toLoc(player.getWorld()).toCenterLocation();
                data.giveHook(player);
            }

            player.teleport(dest);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT , 1.0F, 1.0F);
            player.closeInventory();
            player.sendMessage(
                    Component.translatable("gh.leave",
                            NamedTextColor.GRAY,
                            Component.text("/leave").color(NamedTextColor.YELLOW)
                    )
            );
        }

        @EventHandler(ignoreCancelled = true)
        public void onInventoryClose(InventoryCloseEvent event) {
            if(!event.getPlayer().equals(player)) return;
            if(event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) return;
            HandlerList.unregisterAll(this);
        }
    }
}
