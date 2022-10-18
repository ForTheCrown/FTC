package net.forthecrown.inventory.custom.borders;

import net.forthecrown.inventory.FtcItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GenericBorder extends Border {

    @Override
    public List<Integer> calculateBorderSlots() {
        List<Integer> slots = new ArrayList<>();

        for (int i = 0; i < getSize(); i += 9) { slots.add(i); } // Left
        for (int i = 8; i < getSize(); i += 9) { slots.add(i); } // Right
        for (int i = 1; i < 8; i++) { slots.add(i); } // Top
        for (int i = getSize()-8; i <= getSize()-2; i++) { slots.add(i); } // Bottom

        return slots;
    }

    @Override
    public ItemStack getBorderItem() {
        return FtcItems.makeItem(Material.GRAY_STAINED_GLASS_PANE, 1, true, " ");
    }

}
