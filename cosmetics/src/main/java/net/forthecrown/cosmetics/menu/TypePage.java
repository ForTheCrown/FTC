package net.forthecrown.cosmetics.menu;

import static net.kyori.adventure.text.Component.text;

import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.CosmeticData;
import net.forthecrown.cosmetics.CosmeticType;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.utils.context.Context;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class TypePage<T> extends MenuPage {

  private static final int ROW_TO_SIZE_OFFSET = 1;

  private final CosmeticType<T> type;
  private final TypeDisplay display;

  public TypePage(MenuPage parent, CosmeticType<T> type, TypeDisplay display) {
    super(parent);

    this.type = type;
    this.display = display;

    // Figure out required inventory size
    int lowestRow = 0;
    for (Cosmetic<T> c : type.getCosmetics()) {
      Slot slot = c.getMenuSlot();
      lowestRow = Math.max(lowestRow, slot.getY());
    }

    // +1 to move one row down to give space for border,
    // then apply row->size offset
    int size = Menus.sizeFromRows(lowestRow + 1 + ROW_TO_SIZE_OFFSET);

    initMenu(Menus.builder(size, type.getDisplayName()), true);
  }

  @Override
  public @Nullable ItemStack createItem(@NotNull User user, @NotNull Context context) {
    var builder = ItemStacks.builder(display.material)
        .setName(type.getDisplayName().colorIfAbsent(NamedTextColor.GOLD));

    if (display.description.length > 0) {
      builder.addLore("");

      for (Component component : display.description) {
        builder.addLore(component.colorIfAbsent(NamedTextColor.GRAY));
      }
    }

    return builder.build();
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    for (Cosmetic<T> cosmetic : type.getCosmetics()) {
      builder.add(cosmetic.getMenuSlot(), cosmetic.toMenuNode());
    }

    if (display.noEffectButton) {
      int slot = builder.getSize() - 5;
      builder.add(slot,
          MenuNode.builder()
              .setItem((user, context) -> {
                var item = ItemStacks.builder(Material.BARRIER)
                    .setName(text("No ").append(type.getDisplayName()));

                CosmeticData data = user.getComponent(CosmeticData.class);

                if (data.isUnset(type)) {
                  item.addLore(
                      Text.format("No {0} set", NamedTextColor.GREEN, type.getDisplayName())
                  );

                  item.addEnchant(Enchantment.BINDING_CURSE, 1)
                      .addFlags(ItemFlag.HIDE_ENCHANTS);
                } else {
                  item.addLore("Click to unset current effect");
                }

                return item.build();
              })

              .setRunnable((user, context) -> {
                CosmeticData data = user.getComponent(CosmeticData.class);
                data.set(type, null);
                context.shouldReloadMenu(true);
              })

              .build()
      );
    }
  }
}
