package net.forthecrown.core.admin.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.text.Text;
import net.forthecrown.text.format.UnitFormat;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.concurrent.TimeUnit;

import static net.forthecrown.text.Text.format;
import static net.forthecrown.core.admin.Punishments.INDEFINITE_EXPIRY;
import static net.forthecrown.core.admin.ui.AdminUI.*;
import static net.forthecrown.utils.inventory.menu.Menus.DEFAULT_INV_SIZE;

class TimeSelectionPage extends AdminUiPage {
    private static final int[] MULTIPLIERS = { 1, 2, 5, 10, 20 };

    public TimeSelectionPage(AdminUiPage parent) {
        super(Component.text("Punishment length"), DEFAULT_INV_SIZE, parent);
    }

    @Override
    protected void createMenu(MenuBuilder builder) {
        var start = Slot.of(2, 1);

        for (var o: TimeOption.values()) {
            builder.add(start.add(o.ordinal(), 0),
                    MenuNode.builder()
                            .setItem((user, context) -> {
                                int multiplier = context.get(TIME_MULTIPLIER);

                                return ItemStacks.builder(o.material)
                                        .setName(o.getDisplay(multiplier).style(Text.NON_ITALIC))
                                        .setAmount(multiplier)
                                        .build();
                            })

                            .setRunnable((user, context, click) -> {
                                long length = context.get(TIME_MULTIPLIER) * o.inMillis;

                                var entry = context.get(ENTRY);

                                context.get(PUNISHMENT)
                                        .setLength(length)
                                        .punish(user.getCommandSource(null));

                                AdminUI.open(user, entry.getUser());
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
                                        .setName(
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

    @Getter @RequiredArgsConstructor
    enum TimeOption {
        HOUR ("Hour", TimeUnit.HOURS.toMillis(1), Material.LIME_WOOL),
        DAY ("Day", TimeUnit.DAYS.toMillis(1), Material.YELLOW_WOOL),
        WEEK ("Week", TimeUnit.DAYS.toMillis(7), Material.ORANGE_WOOL),
        MONTH ("Month", TimeUnit.DAYS.toMillis(28), Material.RED_WOOL),

        FOREVER ("Forever", INDEFINITE_EXPIRY, Material.BLACK_WOOL);

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