package net.forthecrown.inventory;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.builder.BuiltInventory;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryBuilder;
import net.forthecrown.inventory.builder.options.InventoryBorder;
import net.forthecrown.inventory.builder.options.InventoryOption;
import net.forthecrown.inventory.builder.options.SimpleOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.Faction;
import net.forthecrown.user.data.Rank;
import net.forthecrown.user.data.RankTier;
import net.forthecrown.utils.ItemStackBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;

/**
 * The RankGUI where players can choose their ranks
 * @deprecated Needs to be rewritten to use {@link net.forthecrown.inventory.builder.BuiltInventory}
 */
@Deprecated
public class RankInventory {

    private final CrownUser user;

    public RankInventory(CrownUser user) {
        this.user = user;
    }

    public BuiltInventory getUsersRankGUI(){
        return switch (user.getHighestTierRank().getTier()) {
            case TIER_1 -> getTier1GUI();
            case TIER_2 -> getTier2GUI();
            case TIER_3 -> getTier3GUI();
            default -> getFreeRanksGUI();
        };
    }



    public BuiltInventory getFreeRanksGUI(){
        /*
        InventoryBuilder builder = new InventoryBuilder(54, Component.text("Free Titles"));
        Inventory inv = getBaseInventory(Faction.ROYALS, "Royals");

        inv.setItem(12, getRankItem(Material.MAP, Rank.KNIGHT, "&7Acquired by completing the first three", "&7levels in the Dungeons and giving the", "&7apples to Diego in the shop."));
        inv.setItem(13, getRankItem(Material.PAPER, Rank.BARON, "&7Acquired by paying 500,000 Rhines."));
        inv.setItem(14, getRankItem(Material.PAPER, Rank.BARONESS, "&7Gotten together with the Baron rank."));

        inv.setItem(29, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.LORD, "&7Included in Tier-1 Donator ranks package,", "&7which costs €10.00 in the webstore."));
        inv.setItem(38, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.LADY, "&7Included in Tier-1 Donator ranks package,", "&7which costs €10.00 in the webstore."));

        inv.setItem(31, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.DUKE, "&7Included in Tier-2 Donator ranks package,", "&7which costs €25.00 in the webstore."));
        inv.setItem(40, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.DUCHESS, "&7Included in Tier-2 Donator ranks package,", "&7which costs €25.00 in the webstore."));

        inv.setItem(33, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.PRINCE, "&7Included in Tier-3 Donator ranks package,", "&7which costs €5.00/month in the webstore."));
        inv.setItem(42, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.PRINCESS, "&7Included in Tier-3 Donator ranks package,", "&7which costs €5.00/month in the webstore."));

        inv.setItem(22, getLegendRank());
        return inv;
        */
        return null;
    }

    public BuiltInventory getTier1GUI() {
        /*
        Inventory inv = getBaseInventory(Faction.PIRATES, "Pirates");

        inv.setItem(13, getLegendRank());

        inv.setItem(21, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.SAILOR, ChatColor.GRAY + "Acquired by gathering 10 pirate points by finding",
                ChatColor.GRAY + "treasures, hunting mob heads or completing",
                ChatColor.GRAY + "some grappling hook challenge levels."));
        inv.setItem(23, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.PIRATE, ChatColor.GRAY + "Acquired by gathering 50 pirate points by finding",
                ChatColor.GRAY + "treasures, hunting mob heads or completing",
                ChatColor.GRAY + "some grappling hook challenge levels."));

        inv.setItem(39, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.CAPTAIN, ChatColor.GRAY + "Included in the Tier 2 Donator ranks package,",
                ChatColor.GRAY + "which costs €25.00 in the webstore."));
        inv.setItem(41, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.ADMIRAL, ChatColor.GRAY + "Included in the Tier 3 Donator ranks package,",
                ChatColor.GRAY + "which costs €6.00/month in the webstore."));

        return inv;*/
        return null;
    }

    public BuiltInventory getTier2GUI() {
        /*
        Inventory inv = getBaseInventory(Faction.VIKINGS, "Vikings");

        inv.setItem(20, getRankItem(Material.PAPER, Rank.VIKING, ""));
        inv.setItem(21, getRankItem(Material.PAPER, Rank.BERSERKER, ""));

        inv.setItem(23, getRankItem(Material.PAPER, Rank.WARRIOR, ""));
        inv.setItem(24, getRankItem(Material.PAPER, Rank.SHIELD_MAIDEN, ""));

        inv.setItem(39, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.HERSIR, ChatColor.GRAY + "Included in the Tier 2 Donator ranks package,",
                ChatColor.GRAY + "which costs €25.00 in the webstore."));
        inv.setItem(41, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.JARL, ChatColor.GRAY + "Included in the Tier 3 Donator ranks package,",
                ChatColor.GRAY + "which costs €5.00/month in the webstore."));

        inv.setItem(13, getLegendRank());

        return inv;*/
        return null;
    }

    public BuiltInventory getTier3GUI() {
        /*
        Inventory inv = getBaseInventory(Faction.VIKINGS, "Vikings");

        inv.setItem(20, getRankItem(Material.PAPER, Rank.VIKING, ""));
        inv.setItem(21, getRankItem(Material.PAPER, Rank.BERSERKER, ""));

        inv.setItem(23, getRankItem(Material.PAPER, Rank.WARRIOR, ""));
        inv.setItem(24, getRankItem(Material.PAPER, Rank.SHIELD_MAIDEN, ""));

        inv.setItem(39, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.HERSIR, ChatColor.GRAY + "Included in the Tier 2 Donator ranks package,",
                ChatColor.GRAY + "which costs €25.00 in the webstore."));
        inv.setItem(41, getRankItem(Material.GLOBE_BANNER_PATTERN, Rank.JARL, ChatColor.GRAY + "Included in the Tier 3 Donator ranks package,",
                ChatColor.GRAY + "which costs €5.00/month in the webstore."));

        inv.setItem(13, getLegendRank());

        return inv;*/
        return null;
    }

    private BuiltInventory getBaseInventory(RankTier tier, TextComponent title){
        InventoryBuilder builder = new InventoryBuilder(54, title)
                .add(new InventoryBorder())
                .add(getNextPageOption(tier))
                .add(4, getRankGUIHeader(tier))
                .add(getDefaultTitleOption());

        // Border decoration
        ItemStack orangeBorder = InventoryBorder.createBorderItem(Material.ORANGE_STAINED_GLASS_PANE);
        ItemStack yellowBorder = InventoryBorder.createBorderItem(Material.YELLOW_STAINED_GLASS_PANE);
        ItemStack blackBorder = InventoryBorder.createBorderItem(Material.BLACK_STAINED_GLASS_PANE);
        return switch (tier) {
            case TIER_1 -> builder.add(3, blackBorder)
                    .add(5, blackBorder)
                    .add(49, blackBorder)
                    .build();
            case TIER_2 -> builder.add(3, orangeBorder)
                    .add(5, orangeBorder)
                    .add(49, orangeBorder)
                    .build();
            case TIER_3 -> builder.add(3, yellowBorder)
                    .add(5, yellowBorder)
                    .add(49, yellowBorder)
                    .add(2, orangeBorder)
                    .add(6, orangeBorder)
                    .add(48, orangeBorder)
                    .add(50, orangeBorder)
                    .build();
            default -> builder.build();
        };
    }

    private ItemStack getLegendRank(){
        if(user.hasRank(Rank.LEGEND) || user.getPlayer().hasPermission("ftc.legend")) return getRankItem(Material.MAP, Rank.LEGEND, "&7The rarest rank on all of FTC");
        return null;
    }

    private ItemStack getRankItem(Material mat, Rank rank, String... description) {
        String name = (rank == Rank.DEFAULT) ? "Default" : rank.getPrefix();

        ItemStack item = new ItemStackBuilder(Material.MAP)
                .setName(name, true)
                .setLore(Arrays.asList(description), true)
                .build();

        if (Rank.freeRanks().contains(rank)) {
            if (user.getAvailableRanks().contains(rank)) item.setType(Material.MAP);
            else item.setType(Material.PAPER);
        }
        if (rank == user.getRank()) item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);

        return item;
    }

    // Header
    private ItemStack getRankGUIHeader(RankTier tier) {
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

    private ItemStack getHeaderItem(Material mat, String itemName, String description, @Nullable Enchantment enchantment) {
        ItemStackBuilder builder = new ItemStackBuilder(mat)
                .setName(Component.text(itemName).style(FtcFormatter.nonItalic(NamedTextColor.AQUA)))
                .addLore(Component.empty())
                .addLore(Component.text(description).style(FtcFormatter.nonItalic(NamedTextColor.GRAY)));
        if (enchantment != null) builder.addEnchant(enchantment, 1);
        return builder.setFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS).build();
    }


    // Next Page
    private SimpleOption getNextPageOption(RankTier tier) {
        return new SimpleOption(8, new ItemStackBuilder(Material.PAPER)
                .setName(Component.text("Next page >").style(FtcFormatter.nonItalic(NamedTextColor.YELLOW)))
                .setFlags(ItemFlag.HIDE_ATTRIBUTES)
                .build(), (user, context) -> getNextInventory(tier).open(user));

    }

    private BuiltInventory getNextInventory(RankTier currentInvTier) {
        return switch (currentInvTier) {
            case TIER_1 -> getTier2GUI();
            case TIER_2 -> getTier3GUI();
            case TIER_3 -> getFreeRanksGUI();
            default -> getTier1GUI();
        };
    }


    // Default title
    private SimpleOption getDefaultTitleOption() {
        ItemStack item = new ItemStackBuilder(Material.MAP)
                .setName(Component.text("Default").style(FtcFormatter.nonItalic(NamedTextColor.WHITE)))
                .addLore(Component.text("This is the default title!").style(FtcFormatter.nonItalic(NamedTextColor.GRAY)))
                .build();
        if (user.getRank() == Rank.DEFAULT) item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);

        return new SimpleOption(10, item, (user, context) -> {
            // TODO: Change to default title
        });
    }

}