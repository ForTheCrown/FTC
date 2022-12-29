package net.forthecrown.inventory;

import static net.forthecrown.utils.inventory.menu.Menus.MAX_INV_SIZE;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import net.forthecrown.user.User;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.user.data.UserRank;
import net.forthecrown.user.data.UserRanks;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.context.ContextOption;
import net.forthecrown.utils.context.ContextSet;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.ClickContext;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.utils.inventory.menu.page.ListPage;
import net.forthecrown.utils.inventory.menu.page.MenuPage;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RankMenu {
  private static final Slot[] DECORATED_SLOTS = {
      Slot.of(3, 0),
      Slot.of(5, 0),
      Slot.of(4, 5),
  };

  private static final ContextSet SET = ContextSet.create();
  private static final ContextOption<Integer> PAGE = SET.newOption(0);

  @Getter
  private static final RankMenu instance = new RankMenu();

  private final RankPage[] menus = new RankPage[RankTier.values().length];

  private RankMenu() {
    for (int i = 0; i < menus.length; i++) {
      if (i == RankTier.NONE.ordinal()) {
        continue;
      }

      RankTier tier = RankTier.values()[i];
      RankPage page = new RankPage(tier);
      menus[i] = page;
    }

    // NONE == FREE menu
    menus[RankTier.NONE.ordinal()] = menus[RankTier.FREE.ordinal()];
  }

  public void open(User user) {
    var tier = user.getTitles().getTier().ordinal();
    open(user, tier);
  }

  private void open(User user, int nextTier) {
    var menu = menus[nextTier % RankTier.values().length];
    menu.getMenu().open(user, SET.createContext());
  }

  public static List<UserRank> getExtraRanks(User user, RankTier tier) {
    return tier.getTitles()
        .stream()
        .filter(rank -> {
          if (rank.getMenuSlot() != null) {
            return false;
          }

          boolean has = user.getTitles().hasTitle(rank);
          return has || !rank.isHidden();
        })
        .collect(Collectors.toList());
  }

  private static void fillSlots(Slot[] slots,
                         MenuBuilder builder,
                         ItemStack itemStack
  ) {
    for (var s: slots) {
      builder.add(s, itemStack);
    }
  }

  private class RankPage extends MenuPage {
    private final RankTier tier;
    private final ExtraRankListPage listPage;

    public RankPage(RankTier tier) {
      super(null);

      this.tier = tier;
      this.listPage = new ExtraRankListPage(this, tier);

      initMenu(
          Menus.builder(MAX_INV_SIZE)
              .setTitle(tier.getDisplayName()),
          true
      );
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
      decorateMenu(builder);
      fillMenu(builder, tier);

      builder.add(8,
          MenuNode.builder()
              .setItem(
                  ItemStacks.builder(Material.PAPER)
                      .setName("&eNext page >")
                      .build()
              )

              .setRunnable((user, context, click) -> {
                click.shouldClose(false);
                click.shouldReloadMenu(false);

                open(user, tier.ordinal() + 1);
              })

              .build()
      );

      builder.add(4, 5, UserRanks.DEFAULT.getMenuNode());
    }

    private void fillMenu(MenuBuilder builder, RankTier tier) {
      tier.getTitles()
          .stream()
          .filter(rank -> rank.getMenuSlot() != null)
          .forEach(rank -> {
            builder.add(rank.getMenuSlot(), rank.getMenuNode());
          });

      for (int i = 1; i < 8; i++) {
        int finalI = i;

        builder.add(i, 4,
            MenuNode.builder()
                .setItem((user, context) -> {
                  var extra = getExtraRanks(user, tier);
                  int index = finalI - 1;

                  if (index >= extra.size()) {
                    return null;
                  }

                  return extra.get(index)
                      .getMenuNode()
                      .createItem(user, context);
                })

                .setRunnable((user, context, click) -> {
                  var extra = getExtraRanks(user, tier);
                  int index = finalI - 1;

                  if (index >= extra.size()) {
                    return;
                  }

                  extra.get(index)
                      .getMenuNode()
                      .onClick(user, context, click);
                })

                .build()
        );
      }

      builder.add(8, 3, listPage);
    }

    private void decorateMenu(MenuBuilder builder) {
      builder.addBorder();

      final ItemStack blackBorder
          = Menus.createBorderItem(Material.BLACK_STAINED_GLASS_PANE);

      final ItemStack goldBorder
          = Menus.createBorderItem(Material.ORANGE_STAINED_GLASS_PANE);

      final ItemStack yellowBorder
          = Menus.createBorderItem(Material.YELLOW_STAINED_GLASS_PANE);

      for (int i = 1; i < 8; i++) {
        builder.add(i, 3, Menus.defaultBorderItem());
      }

      switch (tier) {
        case TIER_3 -> {
          Slot[] golds = {
              Slot.of(2, 0),
              Slot.of(6, 0),
              Slot.of(3, 5),
              Slot.of(5, 5),
          };

          fillSlots(DECORATED_SLOTS, builder, yellowBorder);
          fillSlots(golds, builder, goldBorder);
        }

        case TIER_2 -> {
          fillSlots(DECORATED_SLOTS, builder, yellowBorder);
        }

        case TIER_1 -> {
          fillSlots(DECORATED_SLOTS, builder, blackBorder);
        }
      }
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user,
                                          @NotNull Context context
    ) {
      return switch (tier) {
        case TIER_3 -> ItemStacks.builder(Material.GOLDEN_SWORD)
            .addEnchant(Enchantment.BINDING_CURSE, 1)
            .addFlags(ItemFlag.HIDE_ENCHANTS)
            .build();

        case TIER_2 -> ItemStacks.builder(Material.GOLDEN_SWORD)
            .build();

        case TIER_1 -> ItemStacks.builder(Material.IRON_SWORD)
            .build();

        default -> ItemStacks.builder(Material.STONE)
            .build();
      };
    }

    @Override
    protected MenuNode createHeader() {
      return this;
    }
  }

  private static class ExtraRankListPage extends ListPage<UserRank> {
    private final RankTier tier;

    public ExtraRankListPage(MenuPage parent,
                             RankTier tier
    ) {
      super(parent, PAGE);
      this.tier = tier;

      initMenu(
          Menus.builder(
              Menus.sizeFromRows(5),
              "Extra %s Ranks".formatted(tier.getDisplayName())
          ),
          true
      );
    }

    @Override
    protected List<UserRank> getList(User user, Context context) {
      return getExtraRanks(user, tier);
    }

    @Override
    protected ItemStack getItem(User user, UserRank entry, Context context) {
      return entry.getMenuNode().createItem(user, context);
    }

    @Override
    protected void onClick(User user, UserRank entry, Context context,
                           ClickContext click
    ) throws CommandSyntaxException {
      entry.getMenuNode().onClick(user, context, click);
    }

    @Override
    public @Nullable ItemStack createItem(@NotNull User user,
                                          @NotNull Context context
    ) {

      var extra = getExtraRanks(user, tier);

      if (extra.size() <= 7) {
        return null;
      }

      return ItemStacks.builder(Material.PAPER)
          .setName("&eSee all extra ranks >")
          .addLore(
              Text.format("You have {0, number} non-default ranks",
                  NamedTextColor.GRAY,
                  extra.size()
              )
          )
          .build();
    }

    @Override
    public void onClick(User user, Context context, ClickContext click)
        throws CommandSyntaxException
    {
      var extra = getExtraRanks(user, tier);

      if (extra.size() <= 7) {
        return;
      }

      super.onClick(user, context, click);
    }

    @Override
    protected MenuNode createHeader() {
      return this;
    }
  }
}