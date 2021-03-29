package net.forthecrown.cosmetics.inventories;

import net.forthecrown.core.utils.CrownItems;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomInventory implements InventoryHolder {

    public Inventory inv;

    private int headItemSlot = 4;
    private ItemStack headItem;

    private int returnItemSlot = 0;
    private ItemStack returnItem;

    public CustomInventory(int size, String title, boolean needHead, boolean needReturn) {
        this.inv = Bukkit.createInventory(this, size, Component.text(title));

        if (needHead) this.headItem = CrownItems.makeItem(Material.NETHER_STAR, 1, true, ChatColor.YELLOW + "Menu", "", ChatColor.DARK_GRAY + "ulala");
        else this.headItem = getGlassFiller();

        if (needReturn) this.returnItem = CrownItems.makeItem(Material.PAPER, 1, true, ChatColor.YELLOW + "< Go Back");
        else this.returnItem = getGlassFiller();
    }


    public int getHeadItemSlot() {
        return this.headItemSlot;
    }

    public int getReturnItemSlot() {
        return this.returnItemSlot;
    }


    public void setHeadItemSlot(int newSlot) {
        if (newSlot >= 0 && newSlot < inv.getSize())
            this.headItemSlot = newSlot;
    }

    public void setReturnItemSlot(int newSlot) {
        if (newSlot >= 0 && newSlot < inv.getSize())
            this.returnItemSlot = newSlot;
    }


    public ItemStack getHeadItem() {
        return this.headItem;
    }

    public ItemStack getReturnItem() {
        return this.returnItem;
    }


    public void setHeadItem(ItemStack item) {
        this.headItem = item;
    }

    public void setReturnItem(ItemStack item) {
        this.returnItem = item;
    }


    public ItemStack getGlassFiller() {
        return CrownItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, " ");
    }

    public @NotNull Inventory getInventory() {
        return makeInventory();
    }


    private Inventory makeInventory() {
        Inventory result = this.inv;
        int size = result.getSize();
        ItemStack glass = getGlassFiller();

        for (int i = 0; i < size; i += 9) {
            result.setItem(i, glass);
        }
        for (int i = 8; i < size; i += 9) {
            result.setItem(i, glass);
        }
        for (int i = 1; i < 8; i++) {
            result.setItem(i, glass);
        }
        for (int i = size-8; i <= size-2; i++) {
            result.setItem(i, glass);
        }

        result.setItem(getHeadItemSlot(), getHeadItem());
        result.setItem(getReturnItemSlot(), getReturnItem());
        return result;
    }
}
