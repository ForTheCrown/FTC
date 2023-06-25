package net.forthecrown.antigrief.ui;

import static net.forthecrown.antigrief.ui.AdminUi.ENTRY;
import static net.forthecrown.antigrief.ui.AdminUi.HEADER;
import static net.forthecrown.antigrief.ui.AdminUi.PAGE;
import static net.forthecrown.antigrief.ui.AdminUi.PUNISHMENT;
import static net.forthecrown.text.Text.nonItalic;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.forthecrown.antigrief.PunishType;
import net.forthecrown.antigrief.Punishment;
import net.forthecrown.menu.ClickContext;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.page.ListPage;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PunishmentListPage extends ListPage<Punishment> {

  private final boolean current;

  public PunishmentListPage(MenuPage parent, boolean current) {
    super(parent, PAGE);
    this.current = current;

    initMenu(
        Menus.builder(Component.text((current ? "Current" : "Past") + " punishments")),
        true
    );
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    var builder = ItemStacks.builder(Material.CHEST)
        .setName((current ? "Current" : "Past") + " punishments");

    if (current) {
      builder.addLore("&7Currently active punishments");
    } else {
      builder.addLore("&7Punishments this user has")
          .addLore("&7been given in the past");
    }

    if (getList(user, context).isEmpty()) {
      builder.addLore("&cNo entries to show");
    }

    return builder.build();
  }

  @Override
  protected List<Punishment> getList(User user, Context ctx) {
    var entry = ctx.getOrThrow(ENTRY);
    return current ? entry.getCurrent() : entry.getPast();
  }

  @Override
  protected ItemStack getItem(User user, Punishment punish, Context ctx) {
    return createItem(punish, current);
  }

  static ItemStack createItem(Punishment punish, boolean current) {
    var builder = ItemStacks.builder(typeToMaterial(punish.getType()));

    builder.setNameRaw(
        Text.format("{0} {1}",
            nonItalic(NamedTextColor.WHITE),
            current ? "Active" : "Past",
            punish.getType().presentableName()
        )
    );

    var writer = TextWriters.buffered();
    punish.writeDisplay(writer);

    builder.addLoreRaw(writer.getBuffer());

    return builder.build();
  }

  static Material typeToMaterial(PunishType type) {
    return switch (type) {
      case BAN -> Material.IRON_AXE;
      case JAIL -> Material.IRON_BARS;
      case MUTE -> Material.BARRIER;
      case SOFT_MUTE -> Material.STRUCTURE_VOID;
      case IP_BAN -> Material.DIAMOND_AXE;
      case KICK -> Material.NETHERITE_BOOTS;
    };
  }

  // On entry click
  @Override
  protected void onClick(User user, Punishment entry, Context context, ClickContext click)
      throws CommandSyntaxException
  {
    if (!current) {
      return;
    }

    var node = new PardonPage(this, entry);
    node.getMenu().open(user, context);
  }

  // On page open
  @Override
  public void onClick(User user, Context context, ClickContext click)
      throws CommandSyntaxException
  {
    super.onClick(user, context, click);

    if (current) {
      context.set(PUNISHMENT, null);
    }
  }

  @Override
  protected MenuNode createHeader() {
    return HEADER;
  }
}