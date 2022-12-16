package net.forthecrown.inventory;

import net.forthecrown.utils.inventory.BaseItemBuilder;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface FtcInventory extends Inventory {
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

    default void setItem(Slot pos, ItemStack itemStack) {
        setItem(pos.getIndex(), itemStack);
    }

    default void setItem(Slot pos, BaseItemBuilder builder) {
        setItem(pos.getIndex(), builder.build());
    }

    default void setItem(int slot, BaseItemBuilder builder) {
        setItem(slot, builder.build());
    }

    default ItemStack getItem(Slot pos) {
        return getItem(pos.getIndex());
    }
}