package net.forthecrown.guilds.menu;

import net.forthecrown.guilds.Guilds;
import net.forthecrown.guilds.multiplier.MultiplierType;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiplierMenu extends MenuPage {

  public static final Slot
      SLOT_WEEKEND = Slot.of(6, 2),
      SLOT_GLOBAL = Slot.of(4, 2),
      SLOT_PERSONAL = Slot.of(2, 2),
      SLOT_INFO = Slot.of(4, 4);

  private final MultiplierListMenu globalMenu;
  private final MultiplierListMenu personalMenu;

  public MultiplierMenu(MenuPage parent) {
    super(parent);

    this.personalMenu = new MultiplierListMenu(this, MultiplierType.GUILD);
    this.globalMenu = new MultiplierListMenu(this, MultiplierType.GLOBAL);

    initMenu(Menus.builder(Menus.sizeFromRows(5)).setTitle("EXP multipliers"), true);
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    builder.add(SLOT_GLOBAL, globalMenu);
    builder.add(SLOT_PERSONAL, personalMenu);

    builder.add(
        SLOT_WEEKEND,
        MenuNode.builder()
            .setItem((user, context) -> {
              var modifiers = Guilds.getManager().getExpModifier();
              var config = Guilds.getPlugin().getGuildConfig();

              boolean active = modifiers.isWeekend() && config.weekendMultiplierEnabled;

              var item = ItemStacks.builder(
                  active ? Material.FIRE_CHARGE : Material.FIREWORK_STAR
              );

              if (active) {
                item.addEnchant(Enchantment.BINDING_CURSE, 1).setFlags(ItemFlag.HIDE_ENCHANTS);
              }

              if (config.weekendMultiplierEnabled) {
                item.setName("&eWeekend Multiplier " + config.weekendModifier + "x");
              }

              item.addLore("&7Currently: " + (active ? "&aActive" : "&6Inactive"));
              item.addLore("").addLore("&7Activates for all players during the weekend");

              return item.build();
            })

            .build()
    );

    builder.add(
        SLOT_INFO,

        MenuNode.builder().setItem((user, context) -> {
          return ItemStacks.builder(Material.BOOK)
              .setName("&eInfo")

              .addLore("&7While a 2x multiplier is active, a player that")
              .addLore("&7earns Guild EXP will earn twice the normal amount.")
              .addLore("&7")
              .addLore("&7Note that these multipliers stack, meaning all active")
              .addLore("&7ones are applied at once.")

              .build();
        }).build()
    );
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user,
                                        @NotNull Context context
  ) {
    return ItemStacks.builder(Material.NETHER_STAR)
        .setName("&eEXP Multipliers")
        .build();
  }

  @Override
  protected MenuNode createHeader() {
    return this;
  }
}