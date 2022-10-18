package net.forthecrown.inventory;

import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.Menu;
import net.forthecrown.utils.inventory.menu.MenuBuilder;
import net.forthecrown.utils.inventory.menu.MenuNode;
import net.forthecrown.utils.inventory.menu.Menus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import static net.forthecrown.text.Text.nonItalic;

public final class RankMenu {
    private RankMenu() {}

    private static final ItemStack
            BORDER_ORANGE = Menus.createBorderItem(Material.ORANGE_STAINED_GLASS_PANE),
            BORDER_YELLOW = Menus.createBorderItem(Material.YELLOW_STAINED_GLASS_PANE),
            BORDER_BLACK  = Menus.createBorderItem(Material.BLACK_STAINED_GLASS_PANE );

    /** Option to switch back to default rank */
    private static final MenuNode DEFAULT_OPTION = MenuNode.builder()
            .setItem((user, context) -> {
                var item = ItemStacks.builder(Material.MAP)
                        .setName(Component.text("Default").style(nonItalic(NamedTextColor.WHITE)))
                        .addLore(Component.text("This is the default title!").style(nonItalic(NamedTextColor.GRAY)));

                if (user.getTitles().getTitle() == RankTitle.DEFAULT) {
                    item
                            .setFlags(ItemFlag.HIDE_ENCHANTS)
                            .addEnchant(Enchantment.BINDING_CURSE, 1);
                }

                return item.build();
            })

            .setRunnable((user, context) -> {
                if(user.getTitles().getTitle() == RankTitle.DEFAULT) {
                    throw Exceptions.ALREADY_YOUR_TITLE;
                }

                setTitle(RankTitle.DEFAULT, user);
                context.shouldReloadMenu(true);
            })

            .build();

    private static final ItemStack FREE_HEADER = createHeaderItem(Material.STONE_SWORD,
            "Free Titles",
            "Titles that any player can unlock.",
            null
    );

    private static final ItemStack[] HEADERS = {
            FREE_HEADER, // NONE
            FREE_HEADER, // FREE

            // T 1
            createHeaderItem(Material.IRON_SWORD,
                    "Tier 1 Titles",
                    "Titles that players with Tier 1 rank can unlock.",
                    null
            ),

            // T 2
            createHeaderItem(Material.GOLDEN_SWORD,
                    "Tier 2 Titles",
                    "Titles that players with Tier 2 rank can unlock.",
                    null
            ),

            // T 3
            createHeaderItem(Material.GOLDEN_SWORD,
                    "Tier 3 Titles",
                    "Titles that players with Tier 3 rank can unlock.",
                    Enchantment.DURABILITY
            )
    };

    public static final Menu GUI_FREE_RANKS = createBase(RankTier.FREE, Component.text("Free ranks"))
            .add(2, 2, selectRank(RankTitle.KNIGHT))
            .add(4, 2, selectRank(RankTitle.BARON))
            .add(4, 3, selectRank(RankTitle.BARONESS))
            .add(7, 4, selectRank(RankTitle.LEGACY_FREE))
            .build();

    public static final Menu GUI_TIER1 = createBase(RankTier.TIER_1, Component.text("Tier 1"))
            // Border decorations
            .add(3,  BORDER_BLACK)
            .add(5,  BORDER_BLACK)
            .add(49, BORDER_BLACK)

            // Title options
            .add(2, 2, selectRank(RankTitle.LORD))
            .add(2, 3, selectRank(RankTitle.LADY))
            .add(7, 4, selectRank(RankTitle.LEGACY_TIER_1))
            .build();

    public static final Menu GUI_TIER2 = createBase(RankTier.TIER_2, Component.text("Tier 2"))
            // Border decorations
            .add(3,  BORDER_ORANGE)
            .add(5,  BORDER_ORANGE)
            .add(49, BORDER_ORANGE)

            // Title options
            .add(2, 2, selectRank(RankTitle.DUKE))
            .add(2, 3, selectRank(RankTitle.DUCHESS))
            .add(4, 2, selectRank(RankTitle.CAPTAIN))
            .add(6, 2, selectRank(RankTitle.ELITE))
            .add(7, 4, selectRank(RankTitle.LEGACY_TIER_2))
            .build();

    public static final Menu GUI_TIER3 = createBase(RankTier.TIER_3, Component.text("Tier 3"))
            // Decorations
            .add(3,  BORDER_YELLOW)
            .add(5,  BORDER_YELLOW)
            .add(49, BORDER_YELLOW)
            .add(2,  BORDER_ORANGE)
            .add(6,  BORDER_ORANGE)
            .add(48, BORDER_ORANGE)
            .add(50, BORDER_ORANGE)

            // Title options
            .add(2, 2, selectRank(RankTitle.PRINCE))
            .add(2, 3, selectRank(RankTitle.PRINCESS))
            .add(4, 2, selectRank(RankTitle.ADMIRAL))
            .add(4, 3, selectRank(RankTitle.ROYAL))
            .add(6, 2, selectRank(RankTitle.LEGEND))
            .add(7, 4, selectRank(RankTitle.LEGACY_TIER_3))
            .build();

    private static MenuBuilder createBase(RankTier tier, TextComponent title) {
        return Menus.builder(54, title)
                .addBorder()
                .add(8, 0, getNextPageOption(tier))
                .add(4,    HEADERS[tier.ordinal()])
                .add(1, 1, DEFAULT_OPTION);
    }

    public static void openInventory(User user) {
        switch(user.getTitles().getTier()) {
            case TIER_1 -> GUI_TIER1.open(user);
            case TIER_2 -> GUI_TIER2.open(user);
            case TIER_3 -> GUI_TIER3.open(user);
            default -> GUI_FREE_RANKS.open(user);
        }
    }

    private static ItemStack createHeaderItem(Material mat, String itemName, String description, @Nullable Enchantment enchantment) {
        var builder = ItemStacks.builder(mat)
                .setName(Component.text(itemName).style(nonItalic(NamedTextColor.AQUA)))
                .addLore(Component.empty())
                .addLore(Component.text(description).style(nonItalic(NamedTextColor.GRAY)));

        if (enchantment != null) {
            builder.addEnchant(enchantment, 1);
        }

        return builder.setFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS).build();
    }


    // Next Page
    private static MenuNode getNextPageOption(RankTier tier) {
        return MenuNode.builder()
                .setItem(
                        ItemStacks.builder(Material.PAPER)
                                .setName(Component.text("Next page >").style(nonItalic(NamedTextColor.YELLOW)))
                                .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                                .build()
                )
                .setRunnable((user, context) -> getNextInventory(tier).open(user))
                .build();
    }

    private static Menu getNextInventory(RankTier current) {
        return switch (current) {
            case TIER_1 -> GUI_TIER2;
            case TIER_2 -> GUI_TIER3;
            case TIER_3 -> GUI_FREE_RANKS;
            default -> GUI_TIER1;
        };
    }

    static void setTitle(RankTitle title, User user) {
        user.getTitles().setTitle(title);

        user.sendMessage(
                Text.format("Your title is now {0}",
                        NamedTextColor.GRAY,

                        // Titles have unset main colors,
                        // so ensure the main color of the
                        // title is white
                        Component.text()
                                .color(NamedTextColor.WHITE)
                                .append(title.getTruncatedPrefix())
                                .build()
                )
        );
    }
    
    static MenuNode selectRank(RankTitle title) {
        return MenuNode.builder()
                .setItem((user, context) -> {
                    var item = ItemStacks.builder(user.getTitles().hasTitle(title) ? Material.GLOBE_BANNER_PATTERN : Material.PAPER)
                            .setName(title.getTruncatedPrefix().style(nonItalic(NamedTextColor.WHITE)));

                    if (user.getTitles().getTitle() == title) {
                        item
                                .setFlags(ItemFlag.HIDE_ENCHANTS)
                                .addEnchant(Enchantment.BINDING_CURSE, 1);
                    }

                    return item.build();
                })
                
                .setRunnable((user, context) -> {
                    if(!user.getTitles().hasTitle(title)) {
                        throw Exceptions.DONT_HAVE_TITLE;
                    }

                    if(user.getTitles().getTitle() == title) {
                        throw Exceptions.ALREADY_YOUR_TITLE;
                    }

                    setTitle(title, user);
                    context.shouldReloadMenu(true);
                })
                
                .build();
    }
}