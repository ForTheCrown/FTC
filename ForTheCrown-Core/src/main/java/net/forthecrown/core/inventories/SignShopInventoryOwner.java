package net.forthecrown.core.inventories;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class SignShopInventoryOwner implements InventoryHolder {

    private final Inventory inventory;
    public SignShopInventoryOwner(){
        inventory = Bukkit.createInventory(this, 27, "Shop Contents");
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
