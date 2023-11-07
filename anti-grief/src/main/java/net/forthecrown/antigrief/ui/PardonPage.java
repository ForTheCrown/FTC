package net.forthecrown.antigrief.ui;

import static net.forthecrown.antigrief.ui.AdminUi.ENTRY;
import static net.forthecrown.antigrief.ui.AdminUi.HEADER;
import static net.forthecrown.menu.Menus.DEFAULT_INV_SIZE;

import net.forthecrown.antigrief.Punishment;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PardonPage extends MenuPage {

  private final ItemStack CONFIRM_ITEM = ItemStacks.builder(Material.GREEN_STAINED_GLASS_PANE)
      .setName("Confirm pardon")
      .build();

  private final ItemStack DENY_ITEM = ItemStacks.builder(Material.RED_STAINED_GLASS_PANE)
      .setName("Deny pardon")
      .build();

  private final static Slot CONFIRM_SLOT = Slot.of(3, 1);
  private final static Slot DENY_SLOT = Slot.of(5, 1);

  private final Punishment punishment;

  public PardonPage(MenuPage parent, Punishment punishment) {
    super(parent);
    this.punishment = punishment;

    initMenu(
        Menus.builder(DEFAULT_INV_SIZE, Component.text("Pardon punishment?")),
        true
    );
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    builder.add(HEADER_SLOT.add(0, 2),
        MenuNode.builder()
            // This needs to be a function because when this
            // class is created, this createMenu function will
            // be called before the `punishment` field is set
            .setItem(user -> PunishmentListPage.createItem(punishment, true))
            .build()
    );

    builder.add(CONFIRM_SLOT, option(true));
    builder.add(DENY_SLOT, option(false));
  }

  private MenuNode option(boolean pardon) {
    return MenuNode.builder()
        .setItem((user, context) -> pardon ? CONFIRM_ITEM : DENY_ITEM)

        .setRunnable((user, context, click) -> {
          if (!pardon) {
            getParent().onClick(user, context, click);
            return;
          }

          var entry = context.get(ENTRY);
          entry.revokePunishment(punishment.getType(), user.getName());

          user.sendMessage(
              Text.format("Pardoned {0, user}'s {1}",
                  entry.getHolder(),
                  punishment.getType().presentableName()
              )
          );

          AdminUi.open(user, entry.getUser());
        })

        .build();
  }

  @Override
  protected MenuNode createHeader() {
    return HEADER;
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    return ItemStacks.builder(Material.PAPER)
        .setName("&e< Go back")
        .build();
  }
}