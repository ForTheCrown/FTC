package net.forthecrown.utils.loot;

import com.google.gson.JsonObject;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.JsonUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An easy to create loottable to use in-game
 */
public class LiteralLootTable implements FtcLootTable {

    private final NamespacedKey key;
    private final List<ItemStack> items;

    public LiteralLootTable(NamespacedKey key, ItemStack... items){
        this.key = key;
        this.items = Arrays.asList(items);
    }

    public LiteralLootTable(NamespacedKey key, Collection<ItemStack> items){
        this.key = key;
        this.items = new ArrayList<>(items);
    }

    @Override
    public @NotNull Collection<ItemStack> populateLoot(@NotNull CrownRandom random, @NotNull LootContext context, int maxItems) {
        return random.pickRandomEntries(items, maxItems);
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

    @Override
    public int getMaxItems() {
        return items.size();
    }

    @Override
    public JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.addProperty("key", key.asString());
        json.add("items", JsonUtils.writeCollection(items, JsonUtils::writeItem));

        return json;
    }
}
