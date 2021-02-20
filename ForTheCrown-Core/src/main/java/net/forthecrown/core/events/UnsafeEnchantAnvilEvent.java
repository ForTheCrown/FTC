package net.forthecrown.core.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class UnsafeEnchantAnvilEvent implements Listener {

    int i = 0;

    //TODO: everything lol
    @EventHandler(ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if(event.getInventory().getFirstItem() == null) return;
        if(event.getInventory().getSecondItem() == null) return;
        if(ChatColor.stripColor(event.getInventory().getSecondItem().getLore().get(0)).contains("An enchanted book purchased from Edward")) return;

        i++;
        Bukkit.broadcastMessage("Event called " + i + " times");

        ItemStack result = event.getInventory().getFirstItem().clone();

        for (Enchantment e: event.getInventory().getSecondItem().getEnchantments().keySet()){
            if(!e.getItemTarget().includes(result.getType())) continue;
            result.addUnsafeEnchantment(e, event.getInventory().getSecondItem().getEnchantments().get(e));
        }
        event.setResult(result);
    }
}
