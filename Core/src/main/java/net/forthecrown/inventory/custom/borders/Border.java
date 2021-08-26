package net.forthecrown.inventory.custom.borders;

import net.forthecrown.inventory.custom.SlotClickHandler;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class Border implements SlotClickHandler {

    // This size of the inventory this border belongs to
    private int size = 27;
    public final int getSize() { return this.size; }

    public final Border setSize(int size) {
        this.size = size;
        return this;
    }

    // The inventory slots occupied by the border
    private List<Integer> borderSlots = calculateBorderSlots();
    public final List<Integer> getBorderSlots() { return this.borderSlots; }
    abstract public List<Integer> calculateBorderSlots();

    abstract public ItemStack getBorderItem();

    public final boolean isOnBorder(int slot) {
        return borderSlots.contains(slot);
    }

    public final void applyBorder(@NotNull Inventory inv) {
        setSize(inv.getSize());
        ItemStack borderItem = getBorderItem();

        for (int slot : borderSlots) {
            if (inv.getItem(slot) == null || inv.getItem(slot).getType() == Material.AIR)
                inv.setItem(slot, borderItem);
        }
    }

    public void handleClick(HumanEntity user) {}
}
