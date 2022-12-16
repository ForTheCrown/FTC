package net.forthecrown.events;

import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        // Empty result slot means it's not prepared
        if (ItemStacks.isEmpty(event.getResult())) {
            return;
        }

        ItemStack first = event.getInventory().getFirstItem();
        ItemStack second = event.getInventory().getSecondItem();

        if (ItemStacks.isEmpty(second)) {
            return;
        }

        ItemMeta firstMeta = first.getItemMeta();
        ItemMeta secondMeta = second.getItemMeta();

        // Neither has enchants, we don't care
        if (!firstMeta.hasEnchants() && !secondMeta.hasEnchants()) {
            return;
        }

        ItemStack result = event.getResult();
        ItemMeta resultMeta = result.getItemMeta();
        var resultEnchants = resultMeta.getEnchants();

        for (var e: resultEnchants.entrySet()) {
            var enchant = e.getKey();

            int firstLevel = firstMeta.getEnchantLevel(enchant);
            int secondLevel = secondMeta.getEnchantLevel(enchant);

            // If either level is above max,
            // use the bigger level
            if (firstLevel > enchant.getMaxLevel()
                    || secondLevel > enchant.getMaxLevel()
            ) {
                int level = Math.max(firstLevel, secondLevel);

                // Same level, increment
                if (firstLevel == secondLevel) {
                    level++;
                }

                resultMeta.addEnchant(enchant, level, true);
            }
        }

        result.setItemMeta(resultMeta);
        event.setResult(result);
    }
}