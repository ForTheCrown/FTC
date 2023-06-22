package net.forthecrown.antigrief.ui;

import static net.forthecrown.antigrief.Punishment.INDEFINITE_EXPIRY;
import static net.forthecrown.antigrief.ui.AdminUi.ENTRY;
import static net.forthecrown.antigrief.ui.AdminUi.HEADER;
import static net.forthecrown.antigrief.ui.AdminUi.PUNISHMENT;
import static net.forthecrown.antigrief.ui.AdminUi.TIME_MULTIPLIER;
import static net.forthecrown.menu.Menus.DEFAULT_INV_SIZE;
import static net.forthecrown.text.Text.format;

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.menu.MenuBuilder;
import net.forthecrown.menu.MenuNode;
import net.forthecrown.menu.Menus;
import net.forthecrown.menu.Slot;
import net.forthecrown.menu.page.MenuPage;
import net.forthecrown.text.Text;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

class TimeSelectionPage extends MenuPage {

  private static final int[] MULTIPLIERS = {1, 2, 5, 10, 20};

  public TimeSelectionPage(MenuPage parent) {
    super(parent);
    initMenu(
        Menus.builder(DEFAULT_INV_SIZE, Component.text("Punishment length")),
        true
    );
  }

  @Override
  protected void createMenu(MenuBuilder builder) {
    var start = Slot.of(2, 1);

    for (var o : TimeOption.values()) {
      builder.add(start.add(o.ordinal(), 0),
          MenuNode.builder()
              .setItem((user, context) -> {
                int multiplier = context.getOrThrow(TIME_MULTIPLIER);

                return ItemStacks.builder(o.material)
                    .setNameRaw(o.getDisplay(multiplier).style(Text.NON_ITALIC))
                    .setAmount(multiplier)
                    .build();
              })

              .setRunnable((user, context, click) -> {
                long length = context.getOrThrow(TIME_MULTIPLIER) * o.inMillis;

                var entry = context.get(ENTRY);

                context.getOrThrow(PUNISHMENT)
                    .setLength(length)
                    .punish(user.getCommandSource());

                AdminUi.open(user, entry.getUser());
              })

              .build()
      );
    }

    start = start.add(0, 1);

    for (int i = 0; i < MULTIPLIERS.length; i++) {
      var multiplier = MULTIPLIERS[i];

      builder.add(start.add(i, 0),
          MenuNode.builder()
              .setItem((user, context) -> {
                return ItemStacks.builder(Material.BLACK_STAINED_GLASS_PANE)
                    .setAmount(multiplier)
                    .setNameRaw(
                        format("Time multiplier: {0}x",
                            Text.NON_ITALIC, multiplier
                        )
                    )
                    .build();
              })

              .setRunnable((user, context, click) -> {
                context.set(TIME_MULTIPLIER, multiplier);
                click.shouldReloadMenu(true);
              })

              .build()
      );
    }
  }

  @Override
  protected MenuNode createHeader() {
    return HEADER;
  }

  @Getter
  @RequiredArgsConstructor
  enum TimeOption {
    HOUR("Hour", TimeUnit.HOURS.toMillis(1), Material.LIME_WOOL),
    DAY("Day", TimeUnit.DAYS.toMillis(1), Material.YELLOW_WOOL),
    WEEK("Week", TimeUnit.DAYS.toMillis(7), Material.ORANGE_WOOL),
    MONTH("Month", TimeUnit.DAYS.toMillis(28), Material.RED_WOOL),

    FOREVER("Forever", INDEFINITE_EXPIRY, Material.BLACK_WOOL);

    private final String name;
    private final long inMillis;
    private final Material material;

    public Component getDisplay(int multiplier) {
      if (this == FOREVER) {
        return Component.text("Forever");
      }

      return UnitFormat.unit(multiplier, name);
    }
  }
}