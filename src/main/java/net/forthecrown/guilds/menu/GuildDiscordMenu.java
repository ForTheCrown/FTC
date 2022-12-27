package net.forthecrown.guilds.menu;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.guilds.unlockables.UnlockableDiscordRole;
import net.forthecrown.guilds.unlockables.UnlockableRoleColor;
import net.forthecrown.guilds.unlockables.UnlockableTextChannel;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.ClickContext;
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
        UnlockableDiscordRole.ROLE,
        UnlockableRoleColor.COLOR,
        UnlockableTextChannel.CHANNEL
    );
  }

  @Override
  public void onClick(User user, Context context, ClickContext click)
      throws CommandSyntaxException
  {
    var guild = context.getOrThrow(GUILD);

    if (!UnlockableDiscordRole.ROLE.isUnlocked(guild)) {
      UnlockableDiscordRole.ROLE.toInvOption()
          .onClick(user, context, click);

      return;
    }

    super.onClick(user, context, click);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user,
                                        @NotNull Context context
  ) {
    var guild = context.getOrThrow(GUILD);

    if (!UnlockableDiscordRole.ROLE.isUnlocked(guild)) {
      return UnlockableDiscordRole.ROLE.toInvOption()
          .createItem(user, context);
    }

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