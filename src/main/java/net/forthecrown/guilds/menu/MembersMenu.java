package net.forthecrown.guilds.menu;

import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.forthecrown.guilds.menu.GuildMenus.PAGE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.forthecrown.guilds.GuildMember;
import net.forthecrown.guilds.MemberSort;
import net.forthecrown.guilds.unlockables.Upgradable;
import net.forthecrown.user.User;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.forthecrown.utils.inventory.menu.page.ListPage;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MembersMenu extends ListPage<GuildMember> {

  private static final Style MEMBER_INFO_STYLE = Style.style(NamedTextColor.WHITE)
      .decoration(TextDecoration.ITALIC, false);

  public MembersMenu(MenuPage parent) {
    super(parent, PAGE);

    initMenu(Menus.builder(Menus.MAX_INV_SIZE, "Guild members"), true);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    super.createMenu(builder);

    Arrays.stream(MemberSort.values())
        .forEach(sort -> {
          builder.add(sort.getSlot(), sort.getNode());
        });
  }

  @Override
  protected List<GuildMember> getList(User user, InventoryContext context) {
    var list = context.getOrThrow(GUILD).getMembersList();
    list.sort(user.get(Properties.MEMBER_SORT).getComparator());
    return list;
  }

  @Override
  protected ItemStack getItem(User user, GuildMember member, InventoryContext context) {
    var memberUser = member.getUser();

    var builder = ItemStacks.headBuilder()
        .setProfile(memberUser)
        .setName(memberUser.listDisplayName(user.get(Properties.RANKED_NAME_TAGS)));

    var writer = TextWriters.loreWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));

    writer.field(
        "Guild rank",
        member.getGuild().getMemberPrefix(member.getId())
    );
    writer.field(
        "Member since",
        Text.formatDate(member.getJoinDate())
    );
    writer.field(
        "Total exp earned",
        Text.formatNumber(member.getTotalExpEarned())
    );
    writer.field(
        "Exp earned today",
        Text.formatNumber(member.getExpEarnedToday())
    );
    writer.field(
        "Exp available",
        Text.formatNumber(member.getExpAvailable())
    );

    return builder
        .addLoreRaw(writer.getLore())
        .build();
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull InventoryContext context) {
    var builder = ItemStacks.builder(Material.PLAYER_HEAD)
        .setFlags(ItemFlag.HIDE_ATTRIBUTES)
        .setName("&eGuild Members");

    var item = builder.build();
    var guild = context.getOrThrow(GUILD);

    ItemMeta meta = item.getItemMeta();
    List<Component> lore = new ArrayList<>();

    // This looks like a garbled mess of characters
    lore.add(Component.text("Amount: ", NamedTextColor.GOLD)
        .decoration(TextDecoration.ITALIC, false)
        .append(Component.text(guild.getMemberSize() + "/" +
                Upgradable.MAX_MEMBERS.currentLimit(guild))
            .color(NamedTextColor.WHITE)));
    lore.add(Component.text("Click to view members.")
        .color(NamedTextColor.GRAY)
        .decoration(TextDecoration.ITALIC, false));
    lore.add(Component.text(guild.getSettings().isPublic() ? "Anyone can do /guild join." :
            "Requires an invitation to join guild.")
        .color(NamedTextColor.WHITE)
        .decoration(TextDecoration.ITALIC, false));

    meta.lore(lore);
    item.setItemMeta(meta);
    return item;
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }
}