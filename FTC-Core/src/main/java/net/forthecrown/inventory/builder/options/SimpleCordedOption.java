package net.forthecrown.inventory.builder.options;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.user.CrownUser;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class SimpleCordedOption implements CordedInventoryOption {

    private final InventoryPos pos;
    private final ItemStack item;
    private final InventoryRunnable runnable;

    public SimpleCordedOption(int column, int row, ItemStack item, @Nullable InventoryRunnable runnable) {
        this.pos = new InventoryPos(column, row);
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
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        inventory.setItem(getPos(), getItem());
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
        if(getRunnable() != null) getRunnable().onClick(user, context);
    }
}
