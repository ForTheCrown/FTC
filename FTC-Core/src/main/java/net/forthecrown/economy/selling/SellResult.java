package net.forthecrown.economy.selling;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.commons.lang3.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the result of a user selling some materials from their inventory
 */
public class SellResult {
    public static final int UNLIMITED_TARGET = -1;
    public static SellResult NONE_FOUND = new SellResult(0, 0, null);

    private final int foundAmount;
    private final int targetAmount;

    private final Int2ObjectMap<ItemStack> found;

    public SellResult(int foundAmount, int targetAmount, Int2ObjectMap<ItemStack> found) {
        this.foundAmount = foundAmount;
        this.targetAmount = targetAmount;
        this.found = found;
    }

    /**
     * Creates a sell result out of a given inventory
     *
     * @param inventory The inventory to sell the contents of
     *
     * @param filter    The item filter to use
     *
     * @param material  The material to look for, null will mean
     *                  all found items are added to the result
     *
     * @param amount    The amount of that material to look for,
     *                  must not be 0 or less than -1, a value
     *                  of -1 here means to look for all items,
     *                  aka an unlimited search
     *
     * @return A sell result with the data of what was sold,
     * or a {@link SellResult#NONE_FOUND} if not enough
     * items were found
     */
    public static SellResult create(Inventory inventory, ItemFilter filter, @Nullable Material material, int amount) {
        Validate.isTrue(amount != 0, "Amount cannot be 0");
        Validate.isTrue(amount >= -1, "Amount cannot be less than -1");

        Int2ObjectMap<ItemStack> found = new Int2ObjectOpenHashMap<>();
        int foundAmount = 0;

        // Go through every item, if it matches the type
        // and filter allows, add it to the 'found' map
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            // Test filter first and then type
            // filter has an isEmpty check, so
            // no need to test for that
            if (!filter.test(item)) continue;
            if (material != null && item.getType() != material) continue;

            found.put(i, item);
            foundAmount += item.getAmount();
        }

        // If we didn't find enough items
        if (foundAmount == 0 || foundAmount < amount) {
            return NONE_FOUND;
        }

        return new SellResult(foundAmount, amount, found);
    }

    /**
     * Gets whether the result found anything to sell
     *
     * @return Whether any items where found to sell
     */
    public boolean foundNothing() {
        return getFoundAmount() < 1;
    }

    /**
     * Gets the amount of items that were found
     *
     * @return Found item amount
     */
    public int getFoundAmount() {
        return foundAmount;
    }

    /**
     * Gets the targeted amount of items
     *
     * @return Target amount
     */
    public int getTargetAmount() {
        return targetAmount;
    }

    /**
     * Gets an effective sell amount, aka if the target
     * amount is unlimited (-1) it returns the total
     * amount of found items, but if it's anything else
     * it returns the target amount.
     * <p>
     * If the sell result found nothing, it returns 0
     *
     * @return The effective amount of sell items
     */
    public int getAmount() {
        return sellingUnlimited() ? getFoundAmount() : getTargetAmount();
    }

    /**
     * Gets whether the user sold everything in their inv of the given type
     *
     * @return Whether they sold all items of the material in their inv.
     */
    public boolean sellingUnlimited() {
        return getTargetAmount() == UNLIMITED_TARGET;
    }

    public Int2ObjectMap<ItemStack> getFound() {
        return found;
    }

    /**
     * Removes all the found items from the inventory
     */
    public void removeItems() {
        if (foundNothing()) return;

        int remainingAmount = getTargetAmount();

        for (Int2ObjectMap.Entry<ItemStack> e : getFound().int2ObjectEntrySet()) {
            ItemStack i = e.getValue();

            // If selling is unlimited
            if (remainingAmount == UNLIMITED_TARGET) {
                i.setAmount(0);
                continue;
            }

            int newAmount = i.getAmount() - remainingAmount;

            // If the remaining amount is greater than
            // the item's amount, set the remainingAmount
            // equal to the amount we're missing
            if (newAmount < 0) {
                remainingAmount = -newAmount;
                newAmount = 0;
            } else {
                remainingAmount = 0;
            }

            i.setAmount(newAmount);

            if (remainingAmount < 1) break;
        }
    }
}
