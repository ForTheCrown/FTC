package net.forthecrown.inventory;

import net.forthecrown.core.Worlds;
import net.forthecrown.text.format.UnitFormat;
import net.forthecrown.utils.RomanNumeral;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import static net.forthecrown.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.text;

/**
 * Class for server items, such as Royal Swords, Crowns and home of the great makeItem method
 */
public final class FtcItems {
    private FtcItems() {}

    public static final Material COIN_MATERIAL = Material.SUNFLOWER;

    public static final Style NON_ITALIC_DARK_GRAY = Style.style(NamedTextColor.DARK_GRAY)
            .decoration(TextDecoration.ITALIC, false);

    /**
     * Make some coins
     * @param amount The amount the coin(s) will be worth
     * @param itemAmount The amount of seperate coins to make
     * @return The created coin(s)
     */
    public static ItemStack makeCoins(int amount, int itemAmount) {
        return ItemStacks.builder(COIN_MATERIAL, itemAmount)
                .setName(
                        text(UnitFormat.UNIT_RHINE + "s", nonItalic(NamedTextColor.GOLD))
                )

                .addLore(
                        text("Worth ", nonItalic(NamedTextColor.GOLD))
                                .append(UnitFormat.rhines(amount))
                )

                .addLore(
                        text("Minted in the year " + getOverworldYear() + ".")
                                .style(NON_ITALIC_DARK_GRAY)
                )

                .build();
    }

    private static String getOverworldYear() {
        return RomanNumeral.arabicToRoman(Util.worldTimeToYears(Worlds.overworld()));
    }

}