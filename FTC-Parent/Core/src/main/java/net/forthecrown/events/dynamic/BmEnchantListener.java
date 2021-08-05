package net.forthecrown.events.dynamic;

import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.Balances;
import net.forthecrown.economy.pirates.DailyEnchantment;
import net.forthecrown.economy.pirates.PirateEconomy;
import net.forthecrown.economy.pirates.merchants.EnchantMerchant;
import net.forthecrown.events.Events;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.squire.enchantment.RoyalEnchant;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class BmEnchantListener extends AbstractInvListener implements Listener {

    public BmEnchantListener(Player player) {
        super(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event1) {
        if(!event1.getWhoClicked().equals(player)) return;
        if(event1.getClickedInventory() instanceof PlayerInventory) return;
        if(event1.getSlot() == 11) return;

        event1.setCancelled(true);
        if(event1.getCurrentItem() == null) return;

        ItemStack toCheck = event1.getClickedInventory().getItem(11);
        if(toCheck == null) return;

        Player player = (Player) event1.getWhoClicked();
        Balances balances = ForTheCrown.getBalances();
        PirateEconomy bm = Pirates.getPirateEconomy();

        EnchantMerchant merchant = bm.getEnchantMerchant();
        DailyEnchantment daily = merchant.getDaily();
        Enchantment enchantment = daily.getEnchantment();

        Events.handle(event1.getWhoClicked(), event1, event -> {
            if(!canEnchantItem(toCheck, enchantment)){
                player.openInventory(merchant.createInventory(user, false, toCheck));
                return;
            } else player.openInventory(merchant.createInventory(user, true, toCheck));

            if(event.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE)){
                if(balances.get(player.getUniqueId()) < daily.getPrice()) throw FtcExceptionProvider.cannotAfford(daily.getPrice());
                balances.add(player.getUniqueId(), -daily.getPrice());

                ItemStack toEnchant = event.getClickedInventory().getItem(11).clone();

                if(enchantment instanceof RoyalEnchant) RoyalEnchant.addCrownEnchant(toEnchant, (RoyalEnchant) enchantment, daily.getLevel());
                else {
                    ItemMeta meta = toEnchant.getItemMeta();
                    meta.addEnchant(enchantment, daily.getLevel(), true);
                    toEnchant.setItemMeta(meta);
                }

                try {
                    player.getInventory().addItem(toEnchant);
                } catch (Exception e){
                    player.getWorld().dropItemNaturally(player.getLocation(), toEnchant);
                }
                event.getClickedInventory().setItem(11, null);

                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 1.0f, 1.0f);
                merchant.setAllowedToBuy(player.getUniqueId(), false);

                //Send message
                player.sendMessage(
                        Component.text("You've bought ")
                                .color(NamedTextColor.GRAY)
                                .append(daily.getEnchantment().displayName(daily.getLevel()).color(NamedTextColor.GOLD))
                                .append(Component.text(" for "))
                                .append(FtcFormatter.rhines(daily.getPrice()).color(NamedTextColor.YELLOW))
                );
            }
        });
    }

    private boolean canEnchantItem(ItemStack toEnchant, Enchantment enchantment){
        for (Enchantment e: toEnchant.getEnchantments().keySet()){
            if(e.getKey().equals(enchantment.getKey())) continue;
            if(enchantment.conflictsWith(e)) return false;
        }
        return enchantment.canEnchantItem(toEnchant);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(!event.getPlayer().equals(player)) return;
        if(event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW || event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;

        ItemStack item = event.getInventory().getItem(11);
        if(item != null){
            PlayerInventory inventory = player.getInventory();

            try {
                inventory.addItem(item);
            } catch (Exception e){
                player.getWorld().dropItem(player.getLocation(), item);
            }
        }

        HandlerList.unregisterAll(this);
    }
}
