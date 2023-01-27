package net.forthecrown.guilds.menu;

import net.forthecrown.guilds.unlockables.DiscordUnlocks;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildDiscordMenu extends MenuPage {
  public GuildDiscordMenu(MenuPage parent) {
    super(parent);

    initMenu(
        Menus.builder(Menus.MAX_INV_SIZE - 9, "Discord Menu"),
        true
    );
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    UpgradesMenu.addAll(builder,
        DiscordUnlocks.ROLE,
        DiscordUnlocks.COLOR,
        DiscordUnlocks.CHANNEL
    );
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user,
                                        @NotNull Context context
  ) {
    return ItemStacks.builder(Material.DRAGON_HEAD)
        .setName("&eDiscord Menu")
        .addLore("&7Click to view Discord settings")
        .build();
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }
}