package net.forthecrown.core.inventories;

import net.forthecrown.core.utils.ComponentUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnull;

public class CustomInventoryHolder implements InventoryHolder {

    private final Inventory inv;
    public CustomInventoryHolder(String title, int size){
        inv = Bukkit.createInventory(this, size, ComponentUtils.convertString(title));
    }

    public CustomInventoryHolder(String title, InventoryType type){
        inv = Bukkit.createInventory(this, type, ComponentUtils.convertString(title));
    }

    public CustomInventoryHolder(){
        inv = Bukkit.createInventory(this, InventoryType.CHEST);
    }

    @Nonnull
    @Override
    public Inventory getInventory() {
        return inv;
    }
}
