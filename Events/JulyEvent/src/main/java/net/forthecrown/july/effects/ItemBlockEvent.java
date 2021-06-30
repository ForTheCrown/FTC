package net.forthecrown.july.effects;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemBlockEvent implements BlockEffect {

    private final ItemStack item;
    private final EquipmentSlot slot;

    public ItemBlockEvent(ItemStack item) {
        this.item = item;
        this.slot = null;
    }

    public ItemBlockEvent(ItemStack item, EquipmentSlot slot) {
        this.item = item;
        this.slot = slot;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public @Nullable EquipmentSlot getSlot() {
        return slot;
    }

    @Override
    public void apply(Player player) {
        if(slot == null) player.getInventory().addItem(getItem());
        else player.getInventory().setItem(slot, getItem());
    }

    @Override
    public Material getMaterial() {
        return Material.LIGHT_BLUE_GLAZED_TERRACOTTA;
    }
}
