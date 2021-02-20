package net.forthecrown.core.inventories;

import net.forthecrown.core.CrownUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public abstract class CustomInventory implements InventoryHolder {

    public static final Map<String, CustomInventory> CUSTOM_INVENTORIES = new HashMap<>();

    private final Inventory inv;

    protected CustomInventory(String title, int size){
        CUSTOM_INVENTORIES.put(title, this);

        inv = Bukkit.createInventory(this, size, title);
    }

    protected CustomInventory(){
        inv = null;
    }

    public abstract void onInventoryClick(InventoryClickEvent event);
    //public abstract void onInventoryClose(InventoryCloseEvent event);

    @Nonnull
    protected final Inventory createInventory(String title, int size){
        return Bukkit.createInventory(this, size, CrownUtils.translateHexCodes(title));
    }

    @Nonnull
    @Override
    public Inventory getInventory() {
        return inv;
    }
}
