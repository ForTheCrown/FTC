package net.forthecrown.inventory.weapon.ability.menu;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.forthecrown.utils.inventory.menu.Slot;
import org.bukkit.inventory.ItemStack;

public class AbilitiesMenu {
  public static final Set<Slot> SLOTS = Set.of(
      Slot.of(4, 0),
      Slot.of(6, 1),
      Slot.of(6, 3),
      Slot.of(4, 4),
      Slot.of(2, 3),
      Slot.of(2, 1)
  );

  public static final Slot SWORD_SLOT = Slot.of(4, 2);

  public static final ImmutableSet<Slot> RESERVED_SLOTS
      = ImmutableSet.<Slot>builder()
      .addAll(SLOTS)
      .add(SWORD_SLOT)
      .build();

  public static final Menu MENU = createMenu();

  private static Menu createMenu() {
    var builder = Menus.builder(Menus.sizeFromRows(5));

    for (int i = 0; i < builder.getSize(); i++) {
      Slot s = Slot.of(i);

      if (SLOTS.contains(s) || SWORD_SLOT.equals(s)) {
        builder.add(s,
            MenuNode.builder()
                .setRunnable((user, context) -> {
                  context.cancelEvent(false);
                })
                .build()
        );
      }

      builder.add(s, Menus.defaultBorderItem());
    }

    return builder
        .setCloseCallback((inventory, user, reason) -> {
          for (var s: RESERVED_SLOTS) {
            ItemStack item = inventory.getItem(s);

            if (ItemStacks.isEmpty(item)) {
              continue;
            }

            Util.giveOrDropItem(
                user.getInventory(),
                user.getLocation(),
                item
            );
          }
        })
        .build();
  }
}