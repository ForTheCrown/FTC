package net.forthecrown.guilds.menu;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Objects;
import lombok.Getter;
import net.forthecrown.guilds.GuildExceptions;
import net.forthecrown.guilds.Guilds;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.menu.ClickContext;
import net.forthecrown.utils.context.Context;
import net.forthecrown.menu.page.MenuPage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class MainGuildMenu extends MenuPage {

  private final StatisticsMenu stats;
  private final MessageBoardMenu messageBoard;
  private final UpgradesMenu upgradesMenu;

  public MainGuildMenu() {
    super(null);

    stats = new StatisticsMenu(this);
    messageBoard = new MessageBoardMenu(this);
    upgradesMenu = new UpgradesMenu(this);

    initMenu(
        Menus.builder(Menus.MAX_INV_SIZE - 9, "Guild Menu"),
        false
    );
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    builder.add(Slot.of(2, 2), stats);
    builder.add(Slot.of(3, 2), messageBoard);
    builder.add(Slot.of(5, 2), upgradesMenu);

    builder.add(Slot.of(4, 2),
        MenuNode.builder()
            .setItem((user, context) -> {
              return ItemStacks.builder(Material.KNOWLEDGE_BOOK)
                  .setName("&eChallenges")
                  .addLore("&7Challenges to gain guild Exp.")
                  .build();
            })

            .setRunnable((user, context) -> {
              context.shouldReloadMenu(false);
              user.getPlayer().closeInventory();
              user.getPlayer().performCommand("challenges");
            })

            .build()
    );

    builder.add(Slot.of(6, 2),
        MenuNode.builder()
            .setItem((user, context) -> {
              return ItemStacks.builder(Material.CHEST)
                  .setName(Component.text("Guild Chest", NamedTextColor.YELLOW)
                      .decoration(TextDecoration.ITALIC, false))
                  .addLore(
                      Component.text("A shared inventory for all members.", NamedTextColor.GRAY))
                  .build();
            })

            .setRunnable((user, context, click) -> {
              var guild = context.getOrThrow(GUILD);
              user.getPlayer().openInventory(guild.getInventory());
            })

            .build()
    );
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    var guild = context.getOrThrow(GUILD);
    var formattedBanner = guild.getSettings()
        .getBanner()
        .clone();

    ItemMeta meta = formattedBanner.getItemMeta();
    meta.displayName(Component.text("Guild Menu of ")
        .color(NamedTextColor.WHITE)
        .decoration(TextDecoration.ITALIC, false)
        .append(Component.text(guild.getName())
            .color(guild.getSettings().getPrimaryColor().getTextColor())
        )
    );

    meta.lore(ObjectList.of(
        Component.text("Click on items to navigate the menu.")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false)
    ));

    meta.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS); // Hide applied banner patterns
    formattedBanner.setItemMeta(meta);

    return formattedBanner;
  }

  @Override
  public void onClick(User user, Context context, ClickContext click)
      throws CommandSyntaxException
  {
    var guild = context.getOrThrow(GUILD);

    if (!Objects.equals(Guilds.getGuild(user), guild)) {
      throw GuildExceptions.NOT_IN_GUILD;
    }

    super.onClick(user, context, click);
  }
}