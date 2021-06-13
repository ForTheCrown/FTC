package net.forthecrown.core.utils.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

public interface WeightedLootTable extends CrownLootTable {
    @Override
    @NotNull Collection<ItemStack> populateLoot(@NotNull Random random, @NotNull LootContext context);

    @Override
    void fillInventory(@NotNull Inventory inventory, @NotNull Random random, @NotNull LootContext context);

    @Override
    @NotNull NamespacedKey getKey();

    int getTotalWeight();

    Map<ItemStack, Integer> getItems();

    default float getChance(int weight){
        return ((float) getTotalWeight()) / ((float) weight);
    }

    @Override
    JsonObject serialize();

    static WeightedLootTable of(Key key, Map<ItemStack, Integer> items){
        return new CrownWeightedLootTable(new NamespacedKey(key.namespace(), key.value()), items);
    }

    static WeightedLootTable deserialize(JsonElement json){
        return new CrownWeightedLootTable(json);
    }
}
