package net.forthecrown.utils.inventory.menu.page;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.inventory.menu.context.ClickContext;
import net.forthecrown.utils.inventory.menu.context.ContextOption;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.text.format.page.PageEntryIterator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public abstract class ListPage<T> extends MenuPage {
    @Getter
    private final Slot startSlot;

    private final ContextOption<Integer> page;

    public ListPage(MenuPage parent, ContextOption<Integer> page, Slot startSlot) {
        super(parent);
        this.startSlot = startSlot;
        this.page = Objects.requireNonNull(page);
    }

    public ListPage(MenuPage parent, ContextOption<Integer> page) {
        this(parent, page, Slot.of(1, 1));
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        // Calculate details about how many entries
        // fit onto the page
        Slot endSlot = Slot.of(builder.getSize() - 1);

        /*if (builder.getSize() > (MIN_INV_SIZE * 2)) {
            endSlot = endSlot.add(-1, -1);
        }*/

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
                                var page = getPage(context);
                                var index = ((pageSize * page) + finalI);
                                var list = getList(user, context);

                                // If the current entry's index is an
                                // invalid index, don't place item
                                if (index >= list.size()) {
                                    return null;
                                }

                                return getItem(user, list.get(index), context);
                            })
                            .setRunnable((user, context, click) -> {
                                // Copy-pasted from above :(
                                var page = getPage(context);
                                var index = ((pageSize * page) + finalI);
                                var list = getList(user, context);

                                // If the current entry's index is an
                                // invalid index, don't do anyhing
                                if (index >= list.size()) {
                                    return;
                                }

                                onClick(user, list.get(index), context, click);
                            })
                            .build()
            );
        }
    }

    protected abstract List<T> getList(User user, InventoryContext context);

    protected abstract ItemStack getItem(User user, T entry, InventoryContext context);

    protected void onClick(User user, T entry, InventoryContext context, ClickContext click)
            throws CommandSyntaxException
    {

    }

    protected int getPage(InventoryContext context) {
        return context.getOrThrow(page);
    }
    protected void setPage(int page, InventoryContext context) {
        context.set(this.page, page);
    }

    protected int getMaxPage(User user, InventoryContext context, int pageSize) {
        return PageEntryIterator.getMaxPage(pageSize, getList(user, context).size());
    }

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
                    int maxPage = getMaxPage(user, context, pageSize);
                    var newPage = modifier + getPage(context);

                    // Page change would result in invalid page
                    // Don't add item
                    if (newPage <= 0 || newPage >= maxPage) {
                        return null;
                    }

                    return ItemStacks.builder(Material.PAPER)
                            .setName("&e" + (modifier == -1 ? "< Previous" : "> Next") + " Page")
                            .build();
                })

                .setRunnable((user, context, click) -> {
                    int maxPage = getMaxPage(user, context, pageSize);
                    var newPage = modifier + getPage(context);

                    // Page change would result in invalid page
                    // Don't perform any actions
                    if (newPage <= 0 || newPage >= maxPage) {
                        return;
                    }

                    setPage(newPage, context);
                    click.shouldReloadMenu(true);
                })

                .build();
    }

    @Override
    public void onClick(User user, InventoryContext context, ClickContext click) throws CommandSyntaxException {
        var list = getList(user, context);

        if (list == null || list.isEmpty()) {
            throw Exceptions.NOTHING_TO_LIST;
        }

        setPage(0, context);
        super.onClick(user, context, click);
    }
}