package net.forthecrown.core;

import static net.forthecrown.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.text;

import net.forthecrown.Worlds;
import net.forthecrown.text.RomanNumeral;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

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
   *
   * @param amount     The amount the coin(s) will be worth
   * @param itemAmount The amount of seperate coins to make
   * @return The created coin(s)
   */
  public static ItemStack makeCoins(int amount, int itemAmount) {
    return ItemStacks.builder(COIN_MATERIAL, itemAmount)
        .setNameRaw(
            text(UnitFormat.UNIT_RHINE + "s", nonItalic(NamedTextColor.GOLD))
        )

        .addLoreRaw(
            text("Worth ", nonItalic(NamedTextColor.GOLD))
                .append(UnitFormat.rhines(amount))
        )

        .addLoreRaw(
            text("Minted in the year " + getOverworldYear() + ".")
                .style(NON_ITALIC_DARK_GRAY)
        )

        .build();
  }

  private static String getOverworldYear() {
    return RomanNumeral.arabicToRoman(worldTimeToYears(Worlds.overworld()));
  }

  public static long worldTimeToYears(World world) {
    return ((world.getFullTime() / 1000) / 24) / 365;
  }
}