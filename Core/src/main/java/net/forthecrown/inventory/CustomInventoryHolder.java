package net.forthecrown.inventory;

import net.forthecrown.core.chat.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnull;

/**
 * I'll be honest, I don't know why this exists anymore lol
 * But I know I'm using it lol
 */
@Deprecated
public class CustomInventoryHolder implements InventoryHolder {

    private final Inventory inv;

    @Deprecated
    public CustomInventoryHolder(String title, int size){
        inv = Bukkit.createInventory(this, size, ChatUtils.convertString(title));
    }

    @Deprecated
    public CustomInventoryHolder(String title, InventoryType type){
        inv = Bukkit.createInventory(this, type, ChatUtils.convertString(title));
    }

    @Deprecated
    public CustomInventoryHolder(){
        inv = Bukkit.createInventory(this, InventoryType.CHEST);
    }

    @Nonnull
    @Override
    @Deprecated
    public Inventory getInventory() {
        return inv;
    }
}
