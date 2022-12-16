package net.forthecrown.economy.sell;

import lombok.Getter;
import net.forthecrown.user.data.UserShopData;
import org.bukkit.Material;

/**
 * Represents the data behind an item sell
 * <p>
 * This functions by selling the 'items' 1 item
 * at a time to prevent abuse of item price scaling.
 * The items there are in quotation marks because
 * the items being 'sold' are purely virtual,
 * meaning the only thing that's changing is an
 * integer value, no actual items are being sold
 * or edited.
 * <p>
 * If you want to see how the item sell logic itself
 * is ran, look at {@link #sell()}. If you wish to
 * see how the other part of item selling is done,
 * the part of actually removing items, then see
 * {@link ItemSeller}.
 *
 * @see #sell()
 * @see ItemSeller
 */
@Getter
public class ItemSell {
    /**
     * An unlimited {@link #target} value.
     * <p>
     * Unlimited means it will sell as many items
     * as there is remaining
     */
    public static final int UNLIMITED_TARGET = -1;

    /**
     * The price data used for price and
     * earnings calculation
     */
    private final ItemSellData itemData;

    /**
     * The target amount of items being
     * sold
     */
    private final int target;

    /**
     * The amount of valid 'items' that can be
     * sold.
     */
    private final int itemQuantity;

    /**
     * The price scalar to apply to each item
     * during the sell process.
     * <p>
     * In effect, this basically multiplies the
     * size of the {@link #getItemQuantity()} value
     * to correctly simulate the selling of compact
     * item stacks
     */
    private final int scalar;

    /**
     * The current amount of sold items
     */
    private int sold;

    /**
     * The amount of items that have not yet
     * been sold. Unless any sell logic has
     * been executed, this will be equal to
     * {@link #getItemQuantity()}
     */
    private int remaining;

    /**
     * Gets the amount of rhines earned from selling
     * the current 'items'
     */
    private int earned;

    /**
     * Gets the total amount of rhines earned from selling
     * items of the same type.
     */
    private int totalEarned;

    /**
     * The cached result of the last {@link #sell()} call
     */
    private SellResult cachedResult;

    ItemSell(Material material, ItemSellData itemData, UserShopData earnings, int target, int itemQuantity) {
        this.itemData = itemData;
        this.target = target;
        this.itemQuantity = itemQuantity;

        boolean compacted = material == itemData.getCompactMaterial();
        this.scalar = compacted ? itemData.getCompactMultiplier() : 1;

        this.sold = 0;
        this.earned = 0;
        this.remaining = itemQuantity;
        this.totalEarned = earnings.get(itemData.getMaterial());
    }

    /**
     * Calculates the value of the given material, used for price
     * display calculation in SellShop menus
     * @param mat The material to find the value of
     * @param price The item data of the material
     * @param data The user earnings data
     * @param amount The amount of items to find the value of
     * @return The found sell result
     */
    public static SellResult calculateValue(Material mat, ItemSellData price, UserShopData data, int amount) {
        return new ItemSell(mat, price, data, amount, amount).sell();
    }

    /**
     * Tests if there are enough 'items'
     * to sell anything
     *
     * @return True, if there's enough items
     *         to sell anything
     */
    public boolean hasFoundEnough() {
        if (isUnlimited()) {
            return itemQuantity > 0;
        }

        return itemQuantity >= target;
    }

    /**
     * Tests if this item sell has reached
     * its target sold quantity.
     * <p>
     * If {@link #isUnlimited()} returns true,
     * then this returns the negated value of
     * {@link #hasRemaining()}
     *
     * @return True, if the {@link #getSold()} value
     *         is less than or equal to {@link #getTarget()}
     */
    public boolean hasReachedTarget() {
        if (isUnlimited()) {
            return !hasRemaining();
        }

        return sold >= target;
    }

    /**
     * Tests if this item sell instance is selling
     * all the items it has or is trying to reach
     * a certain goal
     *
     * @return True, if this sell has no end item
     *         quantity target
     */
    public boolean isUnlimited() {
        return getTarget() == UNLIMITED_TARGET;
    }

    /**
     * Tests if there are more items to sell
     * @return True, if there are more items to sell
     */
    public boolean hasRemaining() {
        return remaining > 0;
    }

    /**
     * Runs the sell logic to calculate how many items
     * can be sold and how much can be earned from them
     * <p>
     * If {@link #hasFoundEnough()} returns false, this
     * instantly returns a failed result.
     * <p>
     * This function works by looping for as long as
     * {@link #hasReachedTarget()} returns false and
     * then returning the amount of items sold in the
     * result.
     * <p>
     * In the loop, {@link #calculateSingleItemEarnings()}
     * is called to get the earnings of a single stack,
     * if that returns 0, it means the price has dropped to
     * 0 and we cannot sell anymore and it returns a failed
     * result with how many items it managed to sell before
     * the price dropped to 0.
     * <p>
     * If the loop finishes without the price dropping to 0
     * then this returns a successful sell result
     *
     * @return The result of the selling... great description I know
     * @see SellResult
     */
    public SellResult sell() {
        // We've already run the scan once, return
        // the cached result
        if (cachedResult != null) {
            return cachedResult;
        }

        // If we don't have enough to start with
        if (!hasFoundEnough()) {
            return cachedResult = SellResult.notEnoughItems(this);
        }

        while (!hasReachedTarget()) {
            // Get the amount of rhines we can earn from
            // a single item stack's 1 quantity
            int singleEarnings = calculateSingleItemEarnings();

            // If that method returned 0, that means
            // the price has dropped to 0 and we cannot
            // sell anymore, so return a failed
            // result
            if (singleEarnings == 0) {
                return cachedResult = SellResult.cannotSellMore(this);
            }

            remaining--;
            sold++;
            earned += singleEarnings;
            totalEarned += singleEarnings;
        }

        return cachedResult = SellResult.success(this);
    }

    /**
     * Calculates the value of a single item stack
     * given the current{@link #getTotalEarned()}
     * value.
     * <p>
     * This functions by running a for loop for with
     * {@link #getScalar()} iterations and adding the
     * the item's calculated price onto the result + {@link #getTotalEarned()}.
     * <p>
     * If that calculated price ever reaches 0, it means
     * we cannot fully sell the item stack and our price
     * has dropped to 0
     *
     * @return The calculated value of a single item stack,
     *         or 0, if the price dropped to 0 and the item
     *         could not be sold
     */
    private int calculateSingleItemEarnings() {
        final int earned = this.totalEarned;
        int result = 0;

        for (int i = 0; i < scalar; i++) {
            int single = itemData.calculatePrice(earned + result);

            if (single <= 0) {
                return 0;
            }

            result += single;
        }

        return result;
    }
}