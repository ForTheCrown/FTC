package net.forthecrown.guilds.menu;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.forthecrown.guilds.menu.GuildMenus.PAGE;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import lombok.Getter;
import net.forthecrown.guilds.GuildMessage;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.ClickContext;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.menu.page.ListPage;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class MessageBoardMenu extends ListPage<GuildMessage> {

  private final MessageCreationMenu messageCreation;

  public MessageBoardMenu(MenuPage parent) {
    super(parent, PAGE);

    messageCreation = new MessageCreationMenu(this);

    initMenu(Menus.builder(Menus.MAX_INV_SIZE, "Guild Messages"), true);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    super.createMenu(builder);
    builder.add(8, messageCreation);
  }

  @Override
  protected void onClick(User user,
                         GuildMessage entry,
                         Context context,
                         ClickContext click
  ) throws CommandSyntaxException {
    entry.toMenuNode().onClick(user, context, click);
  }

  @Override
  protected List<GuildMessage> getList(User user, Context context) {
    return context.getOrThrow(GUILD)
        .getMsgBoardPosts();
  }

  @Override
  protected ItemStack getItem(User user, GuildMessage entry, Context context) {
    return entry.toMenuNode().createItem(user, context);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    return ItemStacks.builder(Material.OAK_SIGN)
        .setName("&eMessage Board")
        .addLore("&7A place to post messages for the guild.")
        .build();
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }

  @Override
  public void onClick(User user, Context context, ClickContext click)
      throws CommandSyntaxException {
    setPage(0, context);
    getMenu().open(user, context);
  }
}