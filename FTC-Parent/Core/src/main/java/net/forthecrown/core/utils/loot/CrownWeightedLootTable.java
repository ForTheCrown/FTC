package net.forthecrown.core.utils.loot;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.core.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CrownWeightedLootTable implements WeightedLootTable {

    private final NamespacedKey key;
    private final ImmutableMap<ItemStack, Integer> items;
    private final int totalWeight;

    public CrownWeightedLootTable(NamespacedKey key, Map<ItemStack, Integer> items) {
        this.key = key;
        this.items = ImmutableMap.copyOf(items);
        this.totalWeight = countItemWeights();
    }

    public CrownWeightedLootTable(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        Key temp = CrownUtils.parseKey(json.get("key").getAsString());
        this.key = new NamespacedKey(temp.namespace(), temp.value());

        Map<ItemStack, Integer> tempMap = new HashMap<>();
        JsonArray array = json.getAsJsonArray("items");

        for (JsonElement e: array){
            try {
                JsonObject itemEntry = e.getAsJsonObject();

                int weight = itemEntry.get("weight").getAsInt();
                ItemStack item = JsonUtils.deserializeItem(itemEntry.get("item"));

                tempMap.put(item, weight);
            } catch (CommandSyntaxException e1){
                throw new IllegalStateException("Invalid item in items list");
            }
        }

        this.items = ImmutableMap.copyOf(tempMap);
        this.totalWeight = countItemWeights();
    }

    private int countItemWeights(){
        int total = 0;

        for (Integer i: items.values()){
            total += i;
        }

        return total;
    }

    @Override
    public @NotNull Collection<ItemStack> populateLoot(@NotNull Random random, @NotNull LootContext context) {
        return null;
    }

    @Override
    public void fillInventory(@NotNull Inventory inventory, @NotNull Random random, @NotNull LootContext context) {

    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public int getTotalWeight() {
        return totalWeight;
    }

    @Override
    public Map<ItemStack, Integer> getItems() {
        return items;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.add("key", new JsonPrimitive(key.asString()));

        JsonArray array = new JsonArray();
        for (Map.Entry<ItemStack, Integer> e: items.entrySet()){
            JsonObject itemEntry = new JsonObject();

            itemEntry.add("weight", new JsonPrimitive(e.getValue()));
            itemEntry.add("item", JsonUtils.serializeItem(e.getKey()));

            array.add(itemEntry);
        }

        json.add("items", array);
        return json;
    }
}
