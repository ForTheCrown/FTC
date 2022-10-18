package net.forthecrown.economy.sell;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.text.Messages;
import net.forthecrown.core.Crown;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.user.User;
import net.forthecrown.user.property.Properties;
import net.forthecrown.user.data.UserShopData;
import net.forthecrown.utils.inventory.ItemStacks;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * The Item Seller is the object which handles the actual
 * part of removing items from a user or from a picked up
 * item stack after the {@link ItemSell} has ran its calculations.
 * <p>
 * This class is instantiated by two methods that are both made
 * to cater to the 2 ways a user can sell items to the server
 * sell shop: {@link #inventorySell(User, Material, ItemSellData)} and
 * {@link #itemPickup(User, ItemStack)}.
 * <p>
 * The primary logic is all ran in {@link #run()}
 * @see ItemSell To see how the earnings this class gives
 *      and item removal quantity is calculated
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemSeller {
    /**
     * The user selling items
     */
    private final User user;

    /**
     * The sell calculation instance
     */
    private final ItemSell sell;

    /**
     * The material of the item(s) being sold
     */
    private final Material material;

    /**
     * Inventory index to item map of all
     * the items being sold
     */
    private final Int2ObjectMap<ItemStack> foundItems;

    /**
     * True, if this sell instance was created because
     * of an item being picked up.
     */
    private final boolean autoSell;

    /**
     * Runs the sell logic of this seller.
     * <p>
     * First this calls {@link ItemSell#sell()} to get the result of
     * the selling, if that returns a failed result with 0 sold items,
     * it sends the user the failure message with {@link #sendMessage(Component)}.
     * <p>
     * Otherwise, it gives the earned rhines to the user and also adds
     * the earned amount to the user's earnings list. After that
     * it sends the user the message telling them that they sold the items
     * and tests if the item has dropped price for them, if it has, it
     * tells the user as such.
     */
    public void run() {
        int beforePrice = sell.getItemData().calculatePrice(sell.getTotalEarned()) * sell.getScalar();

        var result = sell.sell();

        // If the amount is 0, that means
        // an exception was thrown before any
        // items could be sold, therefor, send
        // the exception's message to the user
        if (result.getSold() == 0 || !sell.hasReachedTarget()) {
            // Don't use Exceptions#handleSyntaxException, because
            // this might need to send the message to the actionbar
            // of the given seller instead
            sendMessage(GrenadierUtils.formatCommandException(result.getFailure()));
            return;
        }

        int afterPrice = sell.getItemData().calculatePrice(sell.getTotalEarned()) * sell.getScalar();

        // Actually remove the items lol
        removeItems(result);

        user.addBalance(result.getEarned());
        user.getComponent(UserShopData.class)
                .add(sell.getItemData().getMaterial(), result.getEarned());

        sendMessage(Messages.soldItems(result, material));

        // If price dropped
        if (afterPrice < beforePrice) {
            user.sendMessage(Messages.priceDropped(material, beforePrice, afterPrice));
        }
    }

    void removeItems(SellResult result) {
        final int target = result.getSold();
        int removed = 0;

        // Loop through each item in the item map
        // and subtract its quantity until we've
        // removed enough items corresponding to
        // the given sell result
        for (var i: foundItems.values()) {
            if (removed >= target) {
                break;
            }

            var remaining = target - removed;

            // If item quantity is greater than
            // the remaining amount to remove
            if (i.getAmount() > remaining) {
                i.subtract(remaining);
                return;
            } else {
                removed += i.getAmount();
                i.setAmount(0);
            }
        }
    }

    /**
     * Sends the given text either to this seller's
     * chat or action bar depending on the value of
     * {@link #isAutoSell()}
     *
     * @param text The message to send
     */
    void sendMessage(Component text) {
        if (isAutoSell()) {
            user.sendActionBar(text);
        } else {
            user.sendMessage(text);
        }
    }

    /**
     * Creates an item seller from the matching items in the
     * given user's inventory and uses their {@link User#getProperties()}
     * to define if this method should include or skip named items
     * and items with lore.
     *
     * @param user The user selling items
     * @param material The material of the item to sell
     * @param priceData The selling data of the item
     * @return The created seller
     */
    public static ItemSeller inventorySell(User user,
                                           Material material,
                                           ItemSellData priceData
    ) {
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();
        int found = 0;
        int target = user.get(Properties.SELL_AMOUNT).getValue();

        boolean includeNamed = user.get(Properties.SELLING_NAMED_ITEMS);
        boolean includeLore = user.get(Properties.SELLING_LORE_ITEMS);

        var inventory = user.getInventory();
        var it = ItemStacks.nonEmptyIterator(inventory);

        while (it.hasNext()) {
            var index = it.nextIndex();
            var item = it.next();

            if (item.getType() != material) {
                continue;
            }

            var meta = item.getItemMeta();

            // Filter out items using the user's preferences
            if ((meta.hasLore() && !includeLore)
                    || (meta.hasDisplayName() && includeNamed)
            ) {
                continue;
            }

            found += item.getAmount();

            // Do not clone item, we need to original item to
            // be able to modify the base item in the inventory
            items.put(index, item);
        }

        // Create the item sell
        var sell = new ItemSell(
                material,
                priceData,
                user.getComponent(UserShopData.class),
                target, found
        );

        return new ItemSeller(user, sell, material, items, false);
    }

    /**
     * Creates an seller for the picked up item
     * @param user The user picking up the item
     * @param item The item being picked up
     * @return The created seller
     */
    public static ItemSeller itemPickup(User user, ItemStack item) {
        // Holy molly, that getter chain
        var price = Crown.getEconomy()
                .getSellShop()
                .getPriceMap()
                .get(item.getType());

        ItemSell sell = new ItemSell(
                item.getType(),
                price,
                user.getComponent(UserShopData.class),
                item.getAmount(), item.getAmount()
        );

        // Return the seller with a singleton map that has the
        // item we're selling inside it, this way, we don't have
        // to change the system for single items :D
        return new ItemSeller(user, sell, item.getType(), Int2ObjectMaps.singleton(0, item), true);
    }
}