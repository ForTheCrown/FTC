package net.forthecrown.utils.loot;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FtcWeightedLootTable implements WeightedLootTable {

    private final NamespacedKey key;
    private final ImmutableMap<ItemStack, Integer> items;
    private final int totalWeight;
    private final int maxItemsToGive;

    public FtcWeightedLootTable(NamespacedKey key, Map<ItemStack, Integer> items, int maxItemsToGive) {
        this.key = key;
        this.items = ImmutableMap.copyOf(items);
        this.totalWeight = countItemWeights();
        this.maxItemsToGive = maxItemsToGive;
    }

    public FtcWeightedLootTable(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        Key temp = FtcUtils.parseKey(json.get("key").getAsString());
        this.key = new NamespacedKey(temp.namespace(), temp.value());
        this.maxItemsToGive = json.get("maxToGive").getAsInt();

        Map<ItemStack, Integer> tempMap = new HashMap<>();
        JsonArray array = json.getAsJsonArray("items");

        for (JsonElement e: array){
            JsonObject itemEntry = e.getAsJsonObject();

            int weight = itemEntry.get("weight").getAsInt();
            ItemStack item = JsonUtils.readItem(itemEntry.get("item"));

            tempMap.put(item, weight);
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
    public @NotNull Collection<ItemStack> populateLoot(@NotNull CrownRandom random, @NotNull LootContext context, int maxItems) {
        return null;
    }

    @Override
    public void fillInventory(@NotNull Inventory inventory, @NotNull Random random, @NotNull LootContext context) {
        List<ItemStack> items = new ArrayList<>(populateLoot(random, context));

        AtomicInteger index = new AtomicInteger();
        random.ints(items.size(), 0, inventory.getSize()-1)
                .forEach(i -> inventory.setItem(i, items.get(index.getAndIncrement())));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public int getMaxItems() {
        return items.size();
    }

    @Override
    public int getTotalWeight() {
        return totalWeight;
    }

    @Override
    public int getMaxItemsToGive() {
        return maxItemsToGive;
    }

    @Override
    public Map<ItemStack, Integer> getItems() {
        return items;
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.add("maxToGive", new JsonPrimitive(maxItemsToGive));
        json.add("key", new JsonPrimitive(key.asString()));

        JsonArray array = new JsonArray();
        for (Map.Entry<ItemStack, Integer> e: items.entrySet()){
            JsonObject itemEntry = new JsonObject();

            itemEntry.add("weight", new JsonPrimitive(e.getValue()));
            itemEntry.add("item", JsonUtils.writeItem(e.getKey()));

            array.add(itemEntry);
        }

        json.add("items", array);
        return json;
    }
}
