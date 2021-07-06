package net.forthecrown.utils.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.CrownUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public interface CrownLootTable extends LootTable, JsonSerializable {

    @Override
    JsonObject serialize();

    @Override
    default  @NotNull Collection<ItemStack> populateLoot(@NotNull Random random, @NotNull LootContext context){
        return populateLoot(new CrownRandom(random.nextLong()), context, getMaxItems() / 2);
    }

    @NotNull Collection<ItemStack> populateLoot(@NotNull CrownRandom random, @NotNull LootContext context, int maxItems);

    @Override
    void fillInventory(@NotNull Inventory inventory, @NotNull Random random, @NotNull LootContext context);

    @Override
    @NotNull NamespacedKey getKey();

    int getMaxItems();

    static CrownLootTable of(Key key, Collection<ItemStack> items){
        return new LiteralLootTable(new NamespacedKey(key.namespace(), key.value()), items);
    }

    static CrownLootTable of(Key key, ItemStack... items){
        return new LiteralLootTable(new NamespacedKey(key.namespace(), key.value()), items);
    }

    static CrownLootTable deserialize(JsonElement element){
        JsonObject json = element.getAsJsonObject();

        Key key = CrownUtils.parseKey(json.get("key").getAsString());

        JsonArray array = json.getAsJsonArray("items");
        List<ItemStack> items = new ArrayList<>();

        for (JsonElement e: array){
            items.add(JsonUtils.readItem(e));
        }

        return of(key, items);
    }
}
