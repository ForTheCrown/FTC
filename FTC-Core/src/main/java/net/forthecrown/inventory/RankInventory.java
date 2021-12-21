package net.forthecrown.inventory;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.Crown;
import net.forthecrown.core.kingship.Kingship;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.inventory.builder.options.InventoryBorder;
import net.forthecrown.inventory.builder.options.SimpleCordedOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.user.data.RankTitle;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

public final class RankInventory {
    private RankInventory() {}

    private static final ItemStack orangeBorder = InventoryBorder.createBorderItem(Material.ORANGE_STAINED_GLASS_PANE);
    private static final ItemStack yellowBorder = InventoryBorder.createBorderItem(Material.YELLOW_STAINED_GLASS_PANE);
    private static final ItemStack blackBorder = InventoryBorder.createBorderItem(Material.BLACK_STAINED_GLASS_PANE);

    public static void openInventory(CrownUser user) {
        switch(user.getRankTier()) {
            case TIER_1 -> RankInventory.GUI_TIER1.open(user);
            case TIER_2 -> RankInventory.GUI_TIER2.open(user);
            case TIER_3 -> RankInventory.GUI_TIER3.open(user);
            default -> RankInventory.GUI_FREE_RANKS.open(user);
        }
    }

    public static final BuiltInventory GUI_FREE_RANKS = createBase(RankTier.FREE, Component.text("Free ranks"), builder ->
        builder.add(new SelectRankOption(RankTitle.KNIGHT, new InventoryPos(2, 2)))
                .add(new SelectRankOption(RankTitle.BARON, new InventoryPos(4, 2)))
                .add(new SelectRankOption(RankTitle.BARONESS, new InventoryPos(4, 3)))
                .add(new SelectRankOption(RankTitle.LEGACY_FREE, new InventoryPos(7, 4))));

    public static final BuiltInventory GUI_TIER1 = createBase(RankTier.TIER_1, Component.text("Tier 1"), builder -> {
        // Border decorations
        builder.add(3, blackBorder)
                .add(5, blackBorder)
                .add(49, blackBorder);

        // Title options
        builder.add(new SelectRankOption(RankTitle.LORD, new InventoryPos(2, 2)))
                .add(new SelectRankOption(RankTitle.LADY, new InventoryPos(2, 3)))
                .add(new SelectRankOption(RankTitle.LEGACY_TIER_1, new InventoryPos(7, 4)));
    });

    public static final BuiltInventory GUI_TIER2 = createBase(RankTier.TIER_2, Component.text("Tier 2"), builder -> {
        // Border decorations
        builder.add(3, orangeBorder)
                .add(5, orangeBorder)
                .add(49, orangeBorder);

        // Title options
        builder.add(new SelectRankOption(RankTitle.DUKE, new InventoryPos(2, 2)))
                .add(new SelectRankOption(RankTitle.DUCHESS, new InventoryPos(2, 3)))
                .add(new SelectRankOption(RankTitle.CAPTAIN, new InventoryPos(4, 2)))
                .add(new SelectRankOption(RankTitle.ELITE, new InventoryPos(6, 2)))
                .add(new SelectRankOption(RankTitle.LEGACY_TIER_2, new InventoryPos(7, 4)));
    });

    public static final BuiltInventory GUI_TIER3 = createBase(RankTier.TIER_3, Component.text("Tier 3"), builder -> {
        // Border decorations
        builder.add(3, yellowBorder)
                .add(5, yellowBorder)
                .add(49, yellowBorder)
                .add(2, orangeBorder)
                .add(6, orangeBorder)
                .add(48, orangeBorder)
                .add(50, orangeBorder);

        // Title options
        builder.add(new SelectRankOption(RankTitle.PRINCE, new InventoryPos(2, 2)))
                .add(new SelectRankOption(RankTitle.PRINCESS, new InventoryPos(2, 3)))
                .add(new SelectRankOption(RankTitle.ADMIRAL, new InventoryPos(4, 2)))
                .add(new SelectRankOption(RankTitle.ROYAL, new InventoryPos(4, 3)))
                .add(new SelectRankOption(RankTitle.LEGEND, new InventoryPos(6, 2)))
                .add(new SelectRankOption(RankTitle.LEGACY_TIER_3, new InventoryPos(7, 4)));
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

    private static BuiltInventory getNextInventory(RankTier currentInvTier) {
        return switch (currentInvTier) {
            case TIER_1 -> GUI_TIER2;
            case TIER_2 -> GUI_TIER3;
            case TIER_3 -> GUI_FREE_RANKS;
            default -> GUI_TIER1;
        };
    }

    static void setTitle(RankTitle title, CrownUser user) {
        Kingship kingship = Crown.getKingship();
        boolean isKing = user.isKing();
        user.setTitle(title);

        if(isKing) {
            user.sendMessage(
                    Component.translatable("rankSelector.kingWarning", NamedTextColor.GRAY, kingship.getPrefix())
            );
        }

        user.sendMessage(
                Component.translatable("rankSelector.set", NamedTextColor.GRAY,
                        Component.text()
                                .color(NamedTextColor.WHITE)
                                .append(title.noEndSpacePrefix())
                                .build()
                )
        );
    }

    // Default title
    private static CordedInventoryOption getDefaultTitleOption() {
        return new DefaultRankOption(new InventoryPos(1, 1));
    }

    static class DefaultRankOption implements CordedInventoryOption {
        private final InventoryPos pos;

        DefaultRankOption(InventoryPos pos) {
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

            if (user.getTitle() == RankTitle.DEFAULT) {
                item
                        .setFlags(ItemFlag.HIDE_ENCHANTS)
                        .addEnchant(Enchantment.BINDING_CURSE, 1);
            }
            inventory.setItem(pos, item);
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            if(user.getTitle() == RankTitle.DEFAULT) throw FtcExceptionProvider.translatable("rankSelector.alreadySelected");

            setTitle(RankTitle.DEFAULT, user);
            context.setReloadInventory(true);
        }
    }

    static class SelectRankOption implements CordedInventoryOption {
        final RankTitle title;
        private final InventoryPos pos;
        private final List<Component> lore = new ArrayList<>();

        SelectRankOption(RankTitle title, InventoryPos pos, String... description) {
            this.title = title;
            this.pos = pos;

            Arrays.stream(description).forEach(loreString -> {
                if (loreString != null) lore.add(Component.text(loreString).style(nonItalic(NamedTextColor.GRAY)));
            });
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
            ItemStackBuilder item = new ItemStackBuilder(user.hasTitle(title) ? Material.GLOBE_BANNER_PATTERN : Material.PAPER)
                    .setName(title.noEndSpacePrefix().style(nonItalic(NamedTextColor.WHITE)))
                    .addLore(lore);

            if (user.getTitle() == title) {
                item
                        .setFlags(ItemFlag.HIDE_ENCHANTS)
                        .addEnchant(Enchantment.BINDING_CURSE, 1);
            }
            inventory.setItem(pos, item);
        }

        @Override
        public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {
            if(!user.hasTitle(title)) throw FtcExceptionProvider.translatable("rankSelector.notOwned");
            if(user.getTitle() == title) throw FtcExceptionProvider.translatable("rankSelector.alreadySelected");

            setTitle(title, user);
            context.setReloadInventory(true);
        }
    }
}