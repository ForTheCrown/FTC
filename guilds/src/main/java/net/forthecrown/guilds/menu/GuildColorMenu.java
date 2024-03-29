package net.forthecrown.guilds.menu;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

import net.forthecrown.guilds.unlockables.UnlockableColor;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.utils.context.Context;
import net.forthecrown.menu.page.MenuPage;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildColorMenu extends MenuPage {

  private final String title;
  private final boolean primary;

  public GuildColorMenu(MenuPage parent, String title) {
    super(parent);
    this.title = title;
    this.primary = title.contains("Primary");

    initMenu(Menus.builder(Menus.MAX_INV_SIZE, title), true);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    UpgradesMenu.addAll(
        builder, primary
            ? UnlockableColor.getPrimaries()
            : UnlockableColor.getSecondaries()

    );
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    var guild = context.getOrThrow(GUILD);
    var color = primary
        ? guild.getSettings().getPrimaryColor()
        : guild.getSettings().getSecondaryColor();

    return ItemStacks.builder(color.toWool())
        .setName("&e" + title)
        .addLore("&7The " + title + " used in the guild's name format.")
        .build();
  }
}