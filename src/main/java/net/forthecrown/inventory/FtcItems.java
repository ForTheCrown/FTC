package net.forthecrown.inventory;

import static net.forthecrown.utils.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.text;

import net.forthecrown.core.Worlds;
import net.forthecrown.user.Kingship;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.utils.RomanNumeral;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.format.UnitFormat;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Class for server items, such as Royal Swords, Crowns and home of the great makeItem method
 */
public final class FtcItems {

  private FtcItems() {
  }

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
    var builder = ItemStacks.builder(COIN_MATERIAL, itemAmount)
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
        );

    Kingship kingship = UserManager.get().getKingship();
    if (kingship.hasKing()) {
      var user = Users.get(kingship.getKingId());

      String title = switch (kingship.getPreference()) {
        case MONARCH -> "Monarch";
        case QUEEN -> "Queen";
        case KING -> "King";
      };

      builder.addLore(
          Text.format("During the reign of {0} {1}",
              NamedTextColor.DARK_GRAY,
              title,
              user.getName()
          )
      );
    }

    return builder.build();
  }

  private static String getOverworldYear() {
    return RomanNumeral.arabicToRoman(Util.worldTimeToYears(Worlds.overworld()));
  }

}