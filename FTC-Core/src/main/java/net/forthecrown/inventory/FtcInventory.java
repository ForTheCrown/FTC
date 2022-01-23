package net.forthecrown.inventory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.serializer.JsonDeserializable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.JsonUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventoryCustom;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
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

    static FtcInventory wrap(Inventory inventory) {
        return new WrappedFtcInventory(inventory);
    }

    default void setItem(InventoryPos pos, ItemStack itemStack) {
        setItem(pos.getSlot(), itemStack);
    }

    default void setItem(InventoryPos pos, ItemStackBuilder builder) {
        setItem(pos.getSlot(), builder.build());
    }

    default void setItem(int slot, ItemStackBuilder builder) {
        setItem(slot, builder.build());
    }

    default ItemStack getItem(InventoryPos pos) {
        return getItem(pos.getSlot());
    }

    default InventoryPos firstEmptyPos() {
        int first = firstEmpty();

        return first == -1 ? null : InventoryPos.fromSlot(first);
    }

    default boolean isFull() {
        return firstEmpty() == -1;
    }

    default @Nullable Component title() {
        if(this instanceof CraftInventoryCustom custom) {
            return titleFrom(custom);
        }

        return null;
    }

    static Component titleFrom(CraftInventoryCustom custom) {
        try {
            Class mcClass = Class.forName(CraftInventoryCustom.class.getName() + ".MinecraftInventory");
            return (Component) mcClass.getMethod("title").invoke(custom.getInventory());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
    default JsonObject serializeContents() {
        JsonWrapper json = JsonWrapper.empty();

        for (int i = 0; i < getSize(); i++) {
            ItemStack item = getItem(i);
            if(ItemStacks.isEmpty(item)) continue;

            json.addItem(i + "", getItem(i));
        }

        return json.getSource();
    }

    @Override
    default JsonElement serialize() {
        JsonWrapper json = JsonWrapper.empty();

        json.add("size", getSize());
        json.add("contents", serializeContents());

        return json.getSource();
    }
}
