package net.forthecrown.guilds.menu;

import static net.forthecrown.guilds.menu.GuildMenus.DISC_PAGE;
import static net.forthecrown.guilds.menu.GuildMenus.GUILD;
import static net.forthecrown.utils.inventory.menu.Menus.MAX_INV_SIZE;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Objects;
import net.forthecrown.guilds.DiscoverySort;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.user.User;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.inventory.menu.ClickContext;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.menu.page.ListPage;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildDiscoveryMenu extends ListPage<Guild> {
  public static final Slot PUBLIC_ONLY_SLOT = Slot.of(53);

  private final StatisticsMenu statisticsMenu;

  public GuildDiscoveryMenu() {
    super(null, DISC_PAGE);

    this.statisticsMenu = new StatisticsMenu(this);

    initMenu(
        Menus.builder(MAX_INV_SIZE, "Guild Discovery"),
        true
    );
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    super.createMenu(builder);

    for (var s : DiscoverySort.values()) {
      builder.add(s.getSlot(), s.toInvOption());
    }

    builder.add(PUBLIC_ONLY_SLOT,
        MenuNode.builder()
            .setItem((user, context) -> {
              boolean enabled = user.get(Properties.G_DISC_PUBLIC_ONLY);

              var item = ItemStacks.builder(Material.KNOWLEDGE_BOOK)
                  .setName("&eDisplay only public guilds")
                  .addLore("&7Toggle whether private guilds should be shown");

              if (enabled) {
                item.addEnchant(Enchantment.BINDING_CURSE, 1)
                    .addFlags(ItemFlag.HIDE_ENCHANTS)
                    .addLore("Click to show private guilds");
              } else {
                item.addLore("Click to only show public guilds");
              }

              return item.build();
            })

            .setRunnable((user, context, click) -> {
              user.flip(Properties.G_DISC_PUBLIC_ONLY);
              click.shouldReloadMenu(true);
            })

            .build()
    );
  }

  @Override
  protected List<Guild> getList(User user, Context context) {
    List<Guild> list = GuildManager.get().getGuilds();

    boolean invert = user.get(Properties.G_DISC_SORT_INVERTED);
    DiscoverySort sort = user.get(Properties.DISCOVERY_SORT);

    if (user.get(Properties.G_DISC_PUBLIC_ONLY)) {
      list.removeIf(guild -> !guild.getSettings().isPublic());
    }

    list.sort(invert ? sort.reversed() : sort);

    return list;
  }

  @Override
  protected ItemStack getItem(User user,
                              Guild entry,
                              Context context
  ) {
    var builder = ItemStacks.toBuilder(
        entry.getSettings().getBanner().clone()
    );

    builder.clearLore()
        .clearEnchants()
        .setFlags(ItemFlag.HIDE_ITEM_SPECIFICS);

    builder.setName(entry.displayName());

    if (Objects.equals(entry, user.getGuild())) {
      builder.addEnchant(Enchantment.BINDING_CURSE, 1)
          .addFlags(ItemFlag.HIDE_ENCHANTS);
    }

    var writer = TextWriters.loreWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
    writer.setFieldValueStyle(Style.style(NamedTextColor.WHITE));

    entry.writeDiscoverInfo(writer, user);

    builder.addLore(writer.getLore());
    builder.addLore("")
        .addLore("&7Click to view more info");

    return builder.build();
  }

  @Override
  protected void onClick(User user,
                         Guild entry,
                         Context context,
                         ClickContext click
  ) throws CommandSyntaxException {
    context.set(GUILD, entry);
    statisticsMenu.onClick(user, context, click);
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user,
                                        @NotNull Context context
  ) {
    return ItemStacks.builder(Material.COMPASS)
        .setName("&eGuild Discovery Menu")
        .build();
  }

  @Override
  public void onClick(User user, Context context, ClickContext click)
      throws CommandSyntaxException
  {
    setPage(0, context);
    getMenu().open(user, context);
  }
}