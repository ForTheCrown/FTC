package net.forthecrown.utils.inventory.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
@RequiredArgsConstructor
public class BuiltNode implements MenuNode {
    private final MenuNodeItem item;
    private final MenuClickConsumer runnable;

    @Override
    public void onClick(User user, InventoryContext context, ClickContext click) throws CommandSyntaxException {
        if (runnable == null) {
            return;
        }

        runnable.onClick(user, context, click);
    }

    @Override
    public ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        if (item == null) {
            return null;
        }

        return this.item.createItem(user, context);
    }
}