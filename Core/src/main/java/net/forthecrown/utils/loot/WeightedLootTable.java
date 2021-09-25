package net.forthecrown.utils.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Random;

public interface WeightedLootTable extends FtcLootTable {

    @Override
    void fillInventory(@NotNull Inventory inventory, @NotNull Random random, @NotNull LootContext context);

    @Override
    @NotNull NamespacedKey getKey();

    int getTotalWeight();
    int getMaxItemsToGive();

    Map<ItemStack, Integer> getItems();

    default float getChance(int weight){
        return ((float) getTotalWeight()) / ((float) weight);
    }

    @Override
    JsonObject serialize();

    static WeightedLootTable of(Key key, Map<ItemStack, Integer> items, int maxItems){
        return new FtcWeightedLootTable(new NamespacedKey(key.namespace(), key.value()), items, maxItems);
    }

    static WeightedLootTable deserialize(JsonElement json){
        return new FtcWeightedLootTable(json);
    }
}
