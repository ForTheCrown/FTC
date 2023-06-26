package net.forthecrown.cosmetics.menu;

import net.forthecrown.cosmetics.CosmeticType;
import net.forthecrown.menu.Menu;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;

public final class CosmeticMenus {
  private CosmeticMenus() {}

  private static Menu main;

  public static void open(User user) {
    main.open(user);
  }

  public static void createMenus() {
    MenuBuilder builder = Menus.builder(Menus.MAX_INV_SIZE).setTitle("Cosmetics");

    main = builder.build();
  }

  private static MenuNode toNode(CosmeticType<?> type) {
    Menu nodeMenu = createTypeMenu(type);

    return MenuNode.builder()
        .setItem((user, context) -> {
          var builder = ItemStacks.builder(type.getDisplayMaterial())
              .setName(type.getDisplayName());

          return builder.build();
        })

        .setRunnable((user, context) -> {
          nodeMenu.open(user);
        })

        .build();
  }

  private static <T> Menu createTypeMenu(CosmeticType<T> type) {
    MenuBuilder builder = Menus.builder();

    int lowestRow = 0;

    builder.setTitle(type.getDisplayName());

    for (var cosmetic: type.getCosmetics()) {
      builder.add(cosmetic.getMenuSlot(), cosmetic.toMenuNode());

      int row = cosmetic.getMenuSlot().getRow();
      if (row > lowestRow) {
        lowestRow = row;
      }
    }

    int invSize = Menus.sizeFromRows(lowestRow + 1);
    builder.setSize(invSize);
    builder.addBorder();

    return builder.build();
  }
}