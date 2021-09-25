package net.forthecrown.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface FtcInventory extends Inventory, JsonSerializable, JsonDeserializable {
    static FtcInventory of(InventoryHolder holder, int size) {
        return new FtcInventoryImpl(holder, size);
    }

    static FtcInventory of(InventoryHolder holder, InventoryType type) {
        return new FtcInventoryImpl(holder, type);
    }

    static FtcInventory of(InventoryHolder holder, int size, Component title) {
        return new FtcInventoryImpl(holder, size, title);
    }

    static FtcInventory of(InventoryHolder holder, InventoryType type, Component title) {
        return new FtcInventoryImpl(holder, type, title);
    }

    default void setItem(InventoryPos pos, ItemStack itemStack) {
        setItem(pos.getSlot(), itemStack);
    }

    default ItemStack getItem(InventoryPos pos) {
        return getItem(pos.getSlot());
    }

    default InventoryPos firstEmptyPos() {
        int first = firstEmpty();

        return first == -1 ? null : InventoryPos.fromSlot(first);
    }

    @Override
    default void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.of(element.getAsJsonObject());

        for (Map.Entry<String, JsonElement> e: json.entrySet()) {
            int index = Integer.parseInt(e.getKey());
            ItemStack item = JsonUtils.readItem(e.getValue());

            setItem(index, item);
        }
    }

    @Override
    default JsonObject serialize() {
        JsonWrapper json = JsonWrapper.empty();

        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);
            if(FtcUtils.isItemEmpty(item)) continue;

            json.addItem(i + "", getItem(i));
        }

        return json.getSource();
    }
}
