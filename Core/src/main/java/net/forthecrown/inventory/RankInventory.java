package net.forthecrown.inventory;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.inventory.builder.options.InventoryBorder;
import net.forthecrown.inventory.builder.options.SimpleCordedOption;
import net.forthecrown.inventory.builder.options.SimpleOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.Rank;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

public final class RankInventory {
    private RankInventory() {}

    //You can make inventories be these constants so we don't have to remake them everytime someone uses the rank command :D
    public static final BuiltInventory GUI_FREE_RANKS = createBase(RankTier.FREE, Component.text("Free ranks"), builder -> {
        //Create inventory
    });

    private static BuiltInventory createBase(RankTier tier, TextComponent title, Consumer<InventoryBuilder> factory){
        InventoryBuilder builder = new InventoryBuilder(54, title)
                .add(new InventoryBorder())
                .add(getNextPageOption(tier))
                .add(4, getRankGUIHeader(tier))
                .add(getDefaultTitleOption());

        factory.accept(builder);
        return builder.build();
    }

    // Header
    private static ItemStack getRankGUIHeader(RankTier tier) {
        switch (tier) {
            case TIER_1 -> {
                return getHeaderItem(Material.IRON_SWORD,
                        "Tier 1 Titles",
                        "Titles that players with Tier 1 rank can unlock.",
                        null);
            }
            case TIER_2 -> {
                return getHeaderItem(Material.GOLDEN_SWORD,
                        "Tier 2 Titles",
                        "Titles that players with Tier 2 rank can unlock.",
                        null);
            }
            case TIER_3 -> {
                return getHeaderItem(Material.GOLDEN_SWORD,
                        "Tier 3 Titles",
                        "Titles that players with Tier 3 rank can unlock.",
                        Enchantment.DURABILITY);
            }
            default -> {
                return getHeaderItem(Material.STONE_SWORD,
                        "Free Titles",
                        "Titles that any player can unlock.",
                        null);
            }
        }
    }

    private static ItemStack getHeaderItem(Material mat, String itemName, String description, @Nullable Enchantment enchantment) {
        ItemStackBuilder builder = new ItemStackBuilder(mat)
                .setName(Component.text(itemName).style(nonItalic(NamedTextColor.AQUA)))
                .addLore(Component.empty())
                .addLore(Component.text(description).style(nonItalic(NamedTextColor.GRAY)));
        if (enchantment != null) builder.addEnchant(enchantment, 1);
        return builder.setFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS).build();
    }


    // Next Page
    private static SimpleCordedOption getNextPageOption(RankTier tier) {
        return new SimpleCordedOption(8, 0, new ItemStackBuilder(Material.PAPER)
                .setName(Component.text("Next page >").style(nonItalic(NamedTextColor.YELLOW)))
                .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                .build(), (user, context) -> getNextInventory(tier).open(user));
    }

    private BuiltInventory getNextInventory(RankTier currentInvTier) {
        //Needs new terms here

        /*return switch (currentInvTier) {
            case TIER_1 -> getTier2GUI();
            case TIER_2 -> getTier3GUI();
            case TIER_3 -> getFreeRanksGUI();
            default -> getTier1GUI();
        };*/
    }


    // Default title
    private static CordedInventoryOption getDefaultTitleOption() {
        return new DefaultRankOption(new InventoryPos(1, 1));
    }

    static class DefaultRankOption implements CordedInventoryOption {
        private final InventoryPos pos;

        public DefaultRankOption(InventoryPos pos) {
            this.pos = pos;
        }

        @Override
        public InventoryPos getPos() {
            return pos;
        }

        @Override
        public void place(FtcInventory inventory, CrownUser user) {
            ItemStackBuilder item = new ItemStackBuilder(Material.MAP)
                    .setName(Component.text("Default").style(nonItalic(NamedTextColor.WHITE)))
                    .addLore(Component.text("This is the default title!").style(nonItalic(NamedTextColor.GRAY)));


            //getTitle doesn't exist yet lol
            //if(user.getTitle() == RankTitle.DEFAULT) item.addEnchant(Enchantment.BINDING_CURSE, 1);
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            //default selection logic
        }
    }

    static class SelectRankOption implements CordedInventoryOption {
        final RankTitle title;
        private final InventoryPos pos;

        SelectRankOption(RankTitle title, InventoryPos pos) {
            this.title = title;
            this.pos = pos;
        }

        public RankTitle getTitle() {
            return title;
        }

        @Override
        public InventoryPos getPos() {
            return pos;
        }

        @Override
        public void place(FtcInventory inventory, CrownUser user) {
            //place the item that represents the rank
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            //rank selection logic
        }
    }
}