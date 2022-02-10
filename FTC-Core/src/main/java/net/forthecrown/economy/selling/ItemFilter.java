package net.forthecrown.economy.selling;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Predicate;

public class ItemFilter implements Predicate<ItemStack>, JsonSerializable, JsonDeserializable {
    private static final JsonElement
            LORE_ACCEPT_ELEMENT = new JsonPrimitive("acceptLore"),
            NAME_ACCEPT_ELEMENT = new JsonPrimitive("acceptNamed");

    public boolean
            ignoreLore = true,
            ignoreNamed = true;

    // False to ignore, true to accept
    @Override
    public boolean test(ItemStack itemStack) {
        if (ItemStacks.isEmpty(itemStack)) return false;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta.hasLore() && ignoreLore) return false;
        return !meta.hasDisplayName() || !ignoreNamed;
    }

    @Override
    public void deserialize(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            ignoreLore = true;
            ignoreNamed = true;
            return;
        }

        JsonArray array = element.getAsJsonArray();

        this.ignoreLore = !array.contains(LORE_ACCEPT_ELEMENT);
        this.ignoreNamed = !array.contains(NAME_ACCEPT_ELEMENT);
    }

    @Override
    public JsonElement serialize() {
        JsonArray array = new JsonArray();

        if (!ignoreNamed) array.add(NAME_ACCEPT_ELEMENT);
        if (!ignoreLore) array.add(LORE_ACCEPT_ELEMENT);

        return array.isEmpty() ? null : array;
    }
}
