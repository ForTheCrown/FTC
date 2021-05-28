package net.forthecrown.emperor.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * An easy to create loottable to use in-game
 */
public class LiteralLootTable implements LootTable {

    private final NamespacedKey key;
    private final List<ItemStack> items;

    public LiteralLootTable(NamespacedKey key, ItemStack... items){
        this.key = key;
        this.items = Arrays.asList(items);
    }

    @Override
    public @NotNull Collection<ItemStack> populateLoot(@NotNull Random random, @NotNull LootContext context) {
        return items;
    }

    @Override
    public void fillInventory(@NotNull Inventory inventory, @NotNull Random random, @NotNull LootContext context) {
        for (int i = 0; i < inventory.getContents().length; i++){
            if(random.nextBoolean() && random.nextBoolean()){
                ItemStack toPlace = items.get(items.size() == 1 ? 0 : random.nextInt(items.size())).clone();
                if(toPlace.getAmount() > 1){
                    int amount = random.nextInt(toPlace.getAmount())+1;
                    toPlace.setAmount(amount);
                }
                inventory.setItem(i, toPlace);
            }
        }
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
