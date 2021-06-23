package net.forthecrown.julyevent.effects;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ItemGiveEffect implements BlockEffect {

    private final ItemStack item;
    private final EquipmentSlot slot;

    public ItemGiveEffect(ItemStack item) {
        this.item = item.clone();
        this.slot = null;
    }

    public ItemGiveEffect(ItemStack item, EquipmentSlot slot) {
        this.item = item.clone();
        this.slot = slot;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    @Override
    public void apply(Player player) {
        if(slot == null) player.getInventory().addItem(getItem());
        else player.getInventory().setItem(slot, getItem());
    }
}
