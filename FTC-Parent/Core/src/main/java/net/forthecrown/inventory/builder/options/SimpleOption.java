package net.forthecrown.inventory.builder.options;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.user.CrownUser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SimpleOption implements InventoryOption {

    private final int slot;
    private final ItemStack item;

    private final InventoryRunnable runnable;

    public SimpleOption(int slot, ItemStack item, @Nullable InventoryRunnable runnable) {
        this.slot = slot;
        this.item = item;
        this.runnable = runnable;
    }

    public ItemStack getItem() {
        return item;
    }

    public InventoryRunnable getRunnable() {
        return runnable;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        inventory.setItem(slot, item.clone());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        if(runnable != null) runnable.onClick(user, context);
    }
}
