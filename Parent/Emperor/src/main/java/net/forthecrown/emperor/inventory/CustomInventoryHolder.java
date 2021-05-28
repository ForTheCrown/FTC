package net.forthecrown.emperor.inventory;

import net.forthecrown.emperor.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnull;

/**
 * I'll be honest, I don't know why this exists anymore lol
 * But I know I'm using it lol
 */
public class CustomInventoryHolder implements InventoryHolder {

    private final Inventory inv;
    public CustomInventoryHolder(String title, int size){
        inv = Bukkit.createInventory(this, size, ChatUtils.convertString(title));
    }

    public CustomInventoryHolder(String title, InventoryType type){
        inv = Bukkit.createInventory(this, type, ChatUtils.convertString(title));
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
