package net.forthecrown.inventory.builder.options;

import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InvCords;
import net.forthecrown.user.CrownUser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SimpleCordedOption implements CordedInventoryOption {

    private final InvCords coordinates;
    private final ItemStack item;
    private final InventoryRunnable runnable;

    public SimpleCordedOption(int column, int row, ItemStack item, @Nullable InventoryRunnable runnable) {
        this.coordinates = new InvCords(column, row);
        this.item = item;
        this.runnable = runnable;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public InventoryRunnable getRunnable() {
        return runnable;
    }

    @Override
    public InvCords getCoordinates() {
        return coordinates;
    }

    @Override
    public void place(Inventory inventory, CrownUser user) {
        inventory.setItem(getSlot(), getItem());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws RoyalCommandException {
        if(runnable != null) runnable.onClick(user, context);
    }
}
