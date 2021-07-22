package net.forthecrown.august;

import org.apache.commons.lang.math.IntRange;
import org.bukkit.inventory.ItemStack;

public class PinataDrop {
    private final IntRange range;
    private final ItemStack item;

    public PinataDrop(int min, int max, ItemStack itemStack) {
        this.range = new IntRange(min, max);
        this.item = itemStack;
    }

    public IntRange getRange() {
        return range;
    }

    public ItemStack getItem() {
        return item.clone();
    }
}
