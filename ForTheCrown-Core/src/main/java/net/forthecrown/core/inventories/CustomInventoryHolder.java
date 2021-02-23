package net.forthecrown.core.inventories;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CustomInventoryHolder implements InventoryHolder {

    private final Inventory inv;
    public CustomInventoryHolder(String title, int size){
        inv = Bukkit.createInventory(this, size, title);
    }

    public CustomInventoryHolder(String title, InventoryType type){
        inv = Bukkit.createInventory(this, type, title);
    }

    public CustomInventoryHolder(){
        inv = Bukkit.createInventory(this, InventoryType.CHEST);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}
