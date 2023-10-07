package net.forthecrown.sellshop;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.command.Exceptions;
import net.forthecrown.sellshop.event.ItemSellEvent;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemLists;
import net.forthecrown.utils.inventory.ItemStacks;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

/**
 * The Item Seller is the object which handles the actual part of removing items from a user or from
 * a picked up item stack after the {@link ItemSell} has ran its calculations.
 * <p>
 * This class is instantiated by two methods that are both made to cater to the 2 ways a user can
 * sell items to the server sell shop: {@link #inventorySell(User, Material, ItemSellData)} and
 * {@link #itemPickup(User, ItemStack)}.
 * <p>
 * The primary logic is all ran in {@link #run(boolean)}
 *
 * @see ItemSell To see how the earnings this class gives and item removal quantity is calculated
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemSeller {

  /**
   * The user selling items
   */
  private final User player;

  /**
   * The sell calculation instance
   */
  private final ItemSell sell;

  /**
   * The material of the item(s) being sold
   */
  private final Material material;

  /**
   * List of all items being sold
   */
  private final ItemList foundItems;

  /**
   * True, if this sell instance was created because of an item being picked up.
   */
  private final boolean autoSell;

  // Used by the challenge with getEarned() to award points
  private int earned = 0;

  /**
   * Runs the sell logic of this seller.
   * <p>
   * First this calls {@link ItemSell#sell()} to get the result of the selling, if that returns a
   * failed result with 0 sold items, it sends the user the failure message.
   * <p>
   * Otherwise, it gives the earned rhines to the user and also adds the earned amount to the user's
   * earnings list. After that it sends the user the message telling them that they sold the items
   * and tests if the item has dropped price for them, if it has, it tells the user as such.
   *
   * @param send True, to send the item 'You sold X for Y' message, false otherwise
   */
  public SellResult run(boolean send) {
    int beforePrice
        = sell.getItemData().calculatePrice(sell.getTotalEarned())
        * sell.getScalar();

    SellResult result = sell.sell();

    ItemSellEvent event = new ItemSellEvent(
        player, result.getSold(), result.getEarned(), material, result.getFailure()
    );

    event.callEvent();

    // If the amount is 0, that means
    // an exception was thrown before any
    // items could be sold, therefor, send
    // the exception's message to the user
    if (result.getSold() == 0 || !sell.hasReachedTarget()) {
      // Don't use Exceptions#handleSyntaxException, because
      // this might need to send the message to the actionbar
      // of the given seller instead

      Exceptions.handleSyntaxException(player, result.getFailure());
      return result;
    }

    earned = result.getEarned();
    int afterPrice = sell.getItemData().calculatePrice(sell.getTotalEarned()) * sell.getScalar();

    // Actually remove the items lol
    removeItems(result);

    player.addBalance(result.getEarned());
    player.getComponent(UserShopData.class)
        .add(sell.getItemData().getMaterial(), result.getEarned());

    var config = SellShopPlugin.getPlugin().getShopConfig();

    if (!isAutoSell() || !config.logAutoSellOnlyWhenEnds()) {
      log(player, material, result.getSold(), result.getEarned());
    }

    if (send) {
      player.sendMessage(SellMessages.soldItems(result, material));
      player.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }

    // If price dropped
    if (afterPrice < beforePrice) {
      player.sendMessage(SellMessages.priceDropped(material, beforePrice, afterPrice));
    }

    return result;
  }

  public static void log(User player, Material material, int sold, int earned) {
    var config = SellShopPlugin.getPlugin().getShopConfig();

    if (!config.logSelling()) {
      return;
    }

    Loggers.getLogger().info(
        "Selling complete! seller={} material={} quantity={} earned={}",
        player, material, sold, earned
    );
  }

  void removeItems(SellResult result) {
    final int target = result.getSold();
    final int removed = foundItems.removeItems(target);

    if (removed < target) {
      Loggers.getLogger().warn(
          "Tried to remove {} items to sell from {}, only removed {}",
          target, player, removed
      );
    }
  }

  /**
   * Creates an item seller from the matching items in the given user's inventory and uses their
   * properties to define if this method should include or skip named items and items with lore.
   *
   * @param user      The user selling items
   * @param material  The material of the item to sell
   * @param priceData The selling data of the item
   * @return The created seller
   */
  public static ItemSeller inventorySell(User user,
                                         Material material,
                                         ItemSellData priceData
  ) {
    ItemList items = ItemLists.fromInventory(
        user.getInventory(),
        itemStack -> matchesFilters(user, material, itemStack)
    );

    int totalItemCount = items.totalItemCount();

    ItemSell sell = new ItemSell(
        material,
        priceData,
        user.getComponent(UserShopData.class),
        user.get(SellProperties.SELL_AMOUNT).getValue(),
        totalItemCount
    );

    return new ItemSeller(user, sell, material, items, false);
  }

  /**
   * Tests if the given item stack is a valid item for selling selection given the current user's
   * preferences and if it matches the given material
   *
   * @param user      The user selling the item
   * @param material  The material to sell
   * @param itemStack The item to test
   * @return True, if the item can be sold, false otherwise
   */
  public static boolean matchesFilters(User user, Material material, ItemStack itemStack) {
    if (ItemStacks.isEmpty(itemStack)
        || itemStack.getType() != material
    ) {
      return false;
    }

    var meta = itemStack.getItemMeta();

    return (!meta.hasLore() || user.get(SellProperties.SELLING_LORE))
        && (!meta.hasDisplayName() || user.get(SellProperties.SELLING_NAMED));
  }

  /**
   * Creates an seller for the picked up item
   *
   * @param user The user picking up the item
   * @param item The item being picked up
   * @return The created seller
   */
  public static ItemSeller itemPickup(User user, ItemStack item) {
    // Holy molly, that getter chain
    var price = SellShopPlugin.getPlugin()
        .getSellShop()
        .getPriceMap()
        .get(item.getType());

    ItemSell sell = new ItemSell(
        item.getType(),
        price,
        user.getComponent(UserShopData.class),
        item.getAmount(),
        item.getAmount()
    );

    // Return the seller with a singleton map that has the
    // item we're selling inside it, this way, we don't have
    // to change the system for single items :D
    return new ItemSeller(user, sell, item.getType(), ItemLists.newList(item), true);
  }
}