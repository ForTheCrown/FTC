package net.forthecrown.core;

import static net.forthecrown.text.Text.nonItalic;
import static net.kyori.adventure.text.Component.text;

import java.util.Iterator;
import java.util.regex.Pattern;
import net.forthecrown.Worlds;
import net.forthecrown.events.CoinCreationEvent;
import net.forthecrown.events.CoinDepositEvent;
import net.forthecrown.text.RomanNumeral;
import net.forthecrown.text.Text;
import net.forthecrown.text.UnitFormat;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemArrayList;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * Class for server items, such as Royal Swords, Crowns and home of the great makeItem method
 */
public final class Coins {
  private Coins() {}

  public static final Material COIN_MATERIAL = Material.SUNFLOWER;

  public static final Style NON_ITALIC_DARK_GRAY = Style.style(NamedTextColor.DARK_GRAY)
      .decoration(TextDecoration.ITALIC, false);

  private static final Pattern WORTH_PATTERN = Pattern.compile("Worth ([-+]?[\\d,]+) Rhine(?:s|)");

  /**
   * Make some coins
   *
   * @param amount     The amount the coin(s) will be worth
   * @param itemAmount The amount of seperate coins to make
   * @return The created coin(s)
   */
  public static ItemStack makeCoins(int amount, int itemAmount, User user) {
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

    CoinCreationEvent event = new CoinCreationEvent(builder, user, amount);
    event.callEvent();

    return builder.build();
  }

  public static boolean isCoin(ItemStack item) {
    if (ItemStacks.isEmpty(item) || item.getType() != Coins.COIN_MATERIAL) {
      return false;
    }

    var meta = item.getItemMeta();
    var lore = meta.lore();

    if (lore == null || lore.isEmpty()) {
      return false;
    }

    String plain = Text.plain(lore.get(0));

    return WORTH_PATTERN.matcher(plain.trim()).matches();
  }

  public static int getSingleItemValue(ItemStack itemStack) {
    try {
      var lore = itemStack.lore();

      if (lore == null || lore.isEmpty()) {
        return -1;
      }

      Component component = lore.get(0);

      String worth = Text.plain(component).replaceAll("\\D+", "").trim();
      return Integer.parseInt(worth);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /*public static int _deposit(User user, Iterator<ItemStack> it, int maxCoins) {
    int earned = 0;
    int coins = 0;
    int remaining = maxCoins;

    while (it.hasNext()) {
      var item = it.next();

      if (!Coins.isCoin(item)) {
        continue;
      }

      int singleValue = Coins.getSingleItemValue(item);

      if (singleValue == -1) {
        continue;
      }

      int amount = item.getAmount();

      if (amount >= remaining && maxCoins != -1) {
        item.subtract(remaining);
        earned += remaining * singleValue;
        coins += remaining;
        break;
      }

      remaining -= amount;
      earned += singleValue * amount;
      coins += amount;

      item.setAmount(0);
    }

    if (earned == 0) {
      return 0;
    }

    user.addBalance(earned);
    user.sendMessage(CoreMessages.deposit(coins, earned));
    user.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

    return earned;
  }*/

  public static int deposit(User user, Iterator<ItemStack> it, int maxCoins) {
    ItemList allCoins  = collectCoins(it);
    ItemList deposited = new ItemArrayList();

    int trueMax = maxCoins == -1 ? Integer.MAX_VALUE : maxCoins;
    int coins   = 0;
    int earned  = 0;

    for (ItemStack coin : allCoins) {
      int singletonValue = getSingleItemValue(coin);
      int coinAmount = coin.getAmount();
      int nCoins = coins + coinAmount;

      if (nCoins > trueMax) {
        // 'over' is the amount of items that depositing this coin would put us over the
        // max deposit limit, it thus also becomes the remaining amount that will be left
        // after depositing the item
        int over = nCoins - trueMax;
        int itemShrink = coinAmount - over;

        ItemStack cloned = coin.clone();
        cloned.setAmount(itemShrink);
        deposited.add(cloned);

        coin.subtract(itemShrink);

        coins = trueMax;
        earned += singletonValue * itemShrink;
      } else {
        earned += coinAmount * singletonValue;
        coins += coinAmount;

        ItemStack cloned = coin.clone();
        coin.setAmount(0);

        deposited.add(cloned);
      }
    }

    if (earned < 1) {
      return 0;
    }

    CoinDepositEvent event = new CoinDepositEvent(user, deposited, coins, earned);
    event.callEvent();

    if (event.isCancelled() || event.getEarned() < 1) {
      return 0;
    }

    earned = event.getEarned();
    coins = event.getDepositedCoins();

    user.addBalance(earned);
    user.sendMessage(CoreMessages.deposit(coins, earned));
    user.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

    return earned;
  }

  private static ItemList collectCoins(Iterator<ItemStack> it) {
    ItemList list = new ItemArrayList();

    while (it.hasNext()) {
      var n = it.next();

      if (!isCoin(n)) {
        continue;
      }

      list.add(n);
    }

    return list;
  }

  private static String getOverworldYear() {
    return RomanNumeral.arabicToRoman(worldTimeToYears(Worlds.overworld()));
  }

  public static long worldTimeToYears(World world) {
    return ((world.getFullTime() / 1000) / 24) / 365;
  }
}