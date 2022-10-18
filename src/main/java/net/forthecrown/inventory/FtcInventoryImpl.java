package net.forthecrown.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryCustom;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryHolder;

public class FtcInventoryImpl extends CraftInventoryCustom implements FtcInventory {
    public FtcInventoryImpl(InventoryHolder owner, InventoryType type) {
        super(owner, type);
    }

    public FtcInventoryImpl(InventoryHolder owner, InventoryType type, Component title) {
        super(owner, type, title);
    }

    public FtcInventoryImpl(InventoryHolder owner, int size) {
        super(owner, size);
    }

    public FtcInventoryImpl(InventoryHolder owner, int size, Component title) {
        super(owner, size, title);
    }
}