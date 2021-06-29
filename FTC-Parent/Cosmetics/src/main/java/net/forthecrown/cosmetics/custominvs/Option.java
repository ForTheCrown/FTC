package net.forthecrown.cosmetics.custominvs;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Option implements SlotClickHandler {

    private ItemStack item = new ItemStack(Material.OAK_BUTTON, 1);
    public ItemStack getItem() { return this.item; }
    public void setItem(ItemStack item) { this.item = item; }

    @Override
    public void handleClick() {}
}
