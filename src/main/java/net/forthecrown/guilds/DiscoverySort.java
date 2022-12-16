package net.forthecrown.guilds;

import lombok.Getter;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Slot;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.Comparator;

import static net.kyori.adventure.text.Component.text;

@Getter
public enum DiscoverySort implements Comparator<Guild> {
    BY_NAME ("Guild name", Slot.of(8, 1)) {
        @Override
        public int compare(Guild o1, Guild o2) {
            return o1.getName().compareTo(o2.getName());
        }
    },

    BY_AGE ("Guild age", Slot.of(8, 2)) {
        @Override
        public int compare(Guild o1, Guild o2) {
            return Long.compare(
                    o1.getCreationTimeStamp(),
                    o2.getCreationTimeStamp()
            );
        }
    },

    BY_MEMBERS ("#members", Slot.of(8, 3)) {
        @Override
        public int compare(Guild o1, Guild o2) {
            return Integer.compare(
                    o2.getMemberSize(),
                    o1.getMemberSize()
            );
        }
    },

    BY_EXP ("Total Guild Exp", Slot.of(8, 4)) {
        @Override
        public int compare(Guild o1, Guild o2) {
            return Long.compare(o2.getTotalExp(), o1.getTotalExp());
        }
    };

    /** Ascending order arrow */
    public static final String ASC_ARROW = "▲";

    /** Descending order arrow */
    public static final String DES_ARROW = "▼";

    private final String name;
    private final Slot slot;

    DiscoverySort(String name, Slot slot) {
        this.name = "Sort by " + name;
        this.slot = slot;
    }

    @Override
    public Comparator<Guild> reversed() {
        return (o1, o2) -> this.compare(o2, o1);
    }

    public MenuNode toInvOption() {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    var builder = ItemStacks.builder(
                            Material.RED_STAINED_GLASS_PANE
                    );

                    String namePrefix = "";

                    var sort = user.get(Properties.DISCOVERY_SORT);
                    boolean selected = (sort == this);

                    if (selected) {
                        builder.addEnchant(Enchantment.BINDING_CURSE, 1)
                                .setFlags(ItemFlag.HIDE_ENCHANTS);

                        if (user.get(Properties.G_DISC_SORT_INVERTED)) {
                            namePrefix = ASC_ARROW;
                            builder.addLore("Click to sort by descending");
                        } else {
                            namePrefix = DES_ARROW;
                            builder.addLore("Click to sort by ascending");
                        }

                        namePrefix += " ";
                    } else {
                        builder.addLore("Click to select");
                    }

                    builder.addLore("&7Set the order in which")
                            .addLore("&7guilds are displayed");

                    builder.setName(text(
                            namePrefix + getName(),
                            NamedTextColor.AQUA
                    ));

                    return builder.build();
                })

                .setRunnable((user, context, click) -> {
                    var sort = user.get(Properties.DISCOVERY_SORT);
                    boolean selected = (sort == this);

                    if (selected) {
                        user.flip(Properties.G_DISC_SORT_INVERTED);
                    } else {
                        user.set(Properties.G_DISC_SORT_INVERTED, false);
                        user.set(Properties.DISCOVERY_SORT, this);
                    }

                    click.shouldReloadMenu(true);
                })

                .build();
    }
}