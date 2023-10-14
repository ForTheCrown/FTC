package net.forthecrown.menu.page;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.command.Exceptions;
import net.forthecrown.menu.ClickContext;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Slot;
import net.forthecrown.text.page.PagedIterator;
import net.forthecrown.user.User;
import net.forthecrown.user.UserProperty;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public abstract class ListPage<T> extends MenuPage {

  @Getter
  private final Slot startSlot;

  private final ContextOption<Integer> page;

  private SortingOptions<?, T> sortingOptions;

  public ListPage(MenuPage parent, ContextOption<Integer> page, Slot startSlot) {
    super(parent);
    this.startSlot = startSlot;
    this.page = Objects.requireNonNull(page);
  }

  public ListPage(MenuPage parent, ContextOption<Integer> page) {
    this(parent, page, Slot.of(1, 1));
  }

  public <C extends Comparator<T>> void setSortingOptions(SortingOptions<C, T> sortingOptions) {
    this.sortingOptions = sortingOptions;
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    // Calculate details about how many entries
    // fit onto the page
    Slot endSlot = Slot.of(builder.getSize() - 1);

    // Difference between start and end slots
    Slot size = endSlot.add(-startSlot.getX(), -startSlot.getY());

    // entries per page
    int pageSize = size.getX() * size.getY();

    // Page move buttons
    builder.add(
        builder.getSize() - Slot.X_SIZE,
        movePageButton(-1, pageSize)
    );

    builder.add(
        builder.getSize() - 1,
        movePageButton(1, pageSize)
    );

    if (sortingOptions != null) {
      Comparator<T>[] values = sortingOptions.values();
      Slot sortSlot = Slot.of(8, 1);

      for (int i = 0; i < values.length; i++) {
        Comparator<T> val = values[i];
        Slot slot = sortSlot.add(0, i);

        MenuNode node = createSortingSlot(val, (SortingOptions) sortingOptions);
        builder.add(slot, node);
      }
    }

    // Fill page with entries
    // The entries exist constantly, it's just that
    // when there's no entry at the given index, it
    // shows no item and does nothing when clicked
    for (int i = 0; i < pageSize; i++) {
      int column = i % size.getX();
      int row = i / size.getX();

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
                  int middleIndex = (builder.getSize() - 1) / 2;

                  if (slot.getIndex() == middleIndex && list.isEmpty()) {
                    return createEmptyItem(user, context);
                  }

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

  private <C extends Comparator<T>> MenuNode createSortingSlot(
      C option,
      SortingOptions<C, T> options
  ) {
    return MenuNode.builder()
        .setItem((user, context) -> {
          var builder = ItemStacks.builder(Material.RED_STAINED_GLASS_PANE);

          C selected = user.get(options.getProperty());

          boolean isSelected = Objects.equals(selected, option);
          boolean inverted = user.get(options.inversionProperty());

          String namePrefix;

          if (isSelected) {
            builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                .addFlags(ItemFlag.HIDE_ENCHANTS);

            if (inverted) {
              namePrefix = "▲";
              builder.addLore("Click to sort by descending");
            } else {
              namePrefix = "▼";
              builder.addLore("Click to sort by ascending");
            }

            namePrefix += " ";
          } else {
            namePrefix = "";
            builder.addLore("Click to select");
          }

          builder.addLore("&7Set the order in which")
              .addLore("&7" + options.categoryName() + " are displayed");

          builder.setName(
              Component.text(namePrefix + options.displayName(option), NamedTextColor.AQUA)
          );

          return builder.build();
        })

        .setRunnable((user, context, click) -> {
          C selected = user.get(options.getProperty());
          boolean isSelected = Objects.equals(option, selected);

          if (isSelected) {
            user.flip(options.inversionProperty());
          } else {
            user.set(options.getProperty(), option);
            user.set(options.inversionProperty(), false);
          }

          click.shouldReloadMenu(true);
        })
        .build();
  }

  protected abstract List<T> getList(User user, Context context);

  protected abstract ItemStack getItem(User user, T entry, Context context);

  protected void onClick(User user, T entry, Context context, ClickContext click)
      throws CommandSyntaxException
  {

  }

  protected ItemStack createEmptyItem(User user, Context context) {
    return null;
  }

  protected int getPage(Context context) {
    return context.getOrThrow(page);
  }

  protected void setPage(int page, Context context) {
    context.set(this.page, page);
  }

  protected int getMaxPage(User user, Context context, int pageSize) {
    return PagedIterator.getMaxPage(pageSize, getList(user, context).size());
  }

  /**
   * Creates a page move button
   * <p>
   * If the created button's page adjustment would result in a page that's either less than 0 or
   * above the max page then this node will not place an item in the menu and will not accept any
   * click input to adjust the page in that direction.
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
  public void onClick(User user, Context context, ClickContext click)
      throws CommandSyntaxException
  {
    var list = getList(user, context);

    if (list == null || list.isEmpty()) {
      throw Exceptions.NOTHING_TO_LIST;
    }

    setPage(0, context);
    super.onClick(user, context, click);
  }

  public interface SortingOptions<C extends Comparator<T>, T> {
    UserProperty<C> getProperty();

    UserProperty<Boolean> inversionProperty();

    C[] values();

    String displayName(C c);

    String categoryName();
  }
}