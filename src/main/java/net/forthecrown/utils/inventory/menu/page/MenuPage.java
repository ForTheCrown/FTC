package net.forthecrown.utils.inventory.menu.page;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.menu.*;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public abstract class MenuPage implements MenuNode {
    public static final Slot HEADER_SLOT = Slot.of(4);

    @Getter
    private final MenuPage parent;

    @Getter
    private Menu menu;

    protected final void initMenu(MenuBuilder builder, boolean parentButton) {
        addBorder(builder);

        if (parentButton && parent != null) {
            builder.add(Slot.ZERO, parent);
        }

        var header = createHeader();
        if (header != null) {
            builder.add(getHeaderSlot(), header);
        }

        createMenu(builder);

        this.menu = builder.build();
    }

    protected void addBorder(MenuBuilder builder) {
        builder.addBorder();
    }

    protected MenuNode createHeader() {
        return null;
    }

    protected abstract void createMenu(MenuBuilder builder);

    @Override
    public void onClick(User user, InventoryContext context, ClickContext click) throws CommandSyntaxException {
        if (menu == null) {
            return;
        }

        menu.open(user, context);
        user.playSound(Sound.UI_BUTTON_CLICK, 0.4f, 1);
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
        return null;
    }

    protected Slot getHeaderSlot() {
        return HEADER_SLOT;
    }
}