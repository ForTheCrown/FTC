package net.forthecrown.core.admin.ui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.text.format.page.PageEntryIterator;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.*;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static net.forthecrown.core.admin.ui.AdminUI.ENTRY;
import static net.forthecrown.core.admin.ui.AdminUI.PAGE;

abstract class ListUiPage<T> extends AdminUiPage {
    public ListUiPage(Component title, AdminUiPage parent) {
        super(title, Menus.sizeFromRows(4), parent);
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        super.item = createMenuButton();

        // Calculate details about how many entries
        // fit onto the page
        Slot startSlot = Slot.of(1, 1);
        Slot endSlot = Slot.of(builder.getSize() - 1).add(-1, -1);

        // Difference between start and end slots
        Slot size = endSlot.add(-startSlot.getColumn(), -startSlot.getRow());

        // entries per page
        int pageSize = size.getColumn() * size.getRow();

        // Page move buttons
        builder.add(
                builder.getSize() - Slot.COLUMN_SIZE,
                movePageButton(-1, pageSize)
        );

        builder.add(
                builder.getSize() - 1,
                movePageButton(1, pageSize)
        );

        // Fill page with entries
        // The entries exist constantly, it's just that
        // when there's no entry at the given index, it
        // shows no item and does nothing when clicked
        for (int i = 0; i < pageSize; i++) {
            int column = i % size.getColumn();
            int row = i / size.getColumn();

            Slot slot = startSlot.add(column, row);

            final int finalI = i;

            builder.add(slot,
                    MenuNode.builder()
                            .setItem((user, context) -> {
                                // Get list and figure out
                                // what index this is supposed to be
                                var entry = context.get(ENTRY);
                                var page = context.get(PAGE);
                                var index = ((pageSize * page) + finalI);
                                var list = getList(entry);

                                // If the current entry's index is an
                                // invalid index, don't place item
                                if (index >= list.size()) {
                                    return null;
                                }

                                return getItem(list.get(index), entry);
                            })
                            .setRunnable((user, context, click) -> {
                                // Copy-pasted from above
                                var entry = context.get(ENTRY);
                                var page = context.get(PAGE);
                                var index = ((pageSize * page) + finalI);
                                var list = getList(entry);

                                // If the current entry's index is an
                                // invalid index, don't do anyhing
                                if (index >= list.size()) {
                                    return;
                                }

                                onClick(list.get(index), index, user, context);
                            })
                            .build()
            );
        }
    }

    protected abstract List<T> getList(PunishEntry entry);

    protected abstract ItemStack getItem(T entry, PunishEntry punishEntry);
    protected abstract void onClick(T entry, int index, User user, InventoryContext context);

    protected int maxPage(int pageSize, PunishEntry entry) {
        return PageEntryIterator.getMaxPage(pageSize, getList(entry).size());
    }

    /**
     * Creates the item that's used to represent this menu
     * in a different menu. This is mainly used as a button
     * to access this page
     * @return The node representing this page
     */
    protected abstract MenuNodeItem createMenuButton();

    /**
     * Creates a page move button
     * <p>
     * If the created button's page adjustment would result
     * in a page that's either less than 0 or above the max
     * page then this node will not place an item in the menu
     * and will not accept any click input to adjust the page
     * in that direction.
     *
     * @param modifier The direction in which the page should be moved
     * @param pageSize The amount of entries on 1 page
     * @return The created button.
     */
    protected MenuNode movePageButton(int modifier, int pageSize) {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    int maxPage = maxPage(pageSize, context.get(ENTRY));
                    var newPage = modifier + context.get(PAGE);

                    // Page change would result in invalid page
                    // Don't add item
                    if (newPage <= 0 || newPage >= maxPage) {
                        return Menus.defaultBorderItem();
                    }

                    return ItemStacks.builder(Material.PAPER)
                            .setName("&e" + (modifier == -1 ? "< Previous" : "> Next") + " Page")
                            .build();
                })

                .setRunnable((user, context, click) -> {
                    int maxPage = maxPage(pageSize, context.get(ENTRY));
                    var newPage = modifier + context.get(PAGE);

                    // Page change would result in invalid page
                    // Don't perform any actions
                    if (newPage <= 0 || newPage >= maxPage) {
                        return;
                    }

                    context.set(PAGE, newPage);
                    click.shouldReloadMenu(true);
                })
                .build();
    }

    @Override
    public void onClick(User user, InventoryContext context, ClickContext click) throws CommandSyntaxException {
        if (getList(context.get(ENTRY)).isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
        }

        context.set(PAGE, 0);
        super.onClick(user, context, click);
    }
}