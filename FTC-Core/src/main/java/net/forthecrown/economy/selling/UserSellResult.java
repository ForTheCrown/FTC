package net.forthecrown.economy.selling;

import net.forthecrown.user.CrownUser;
import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;

/**
 * Represents the result of a user selling some materials from his inventory
 */
public class UserSellResult {
    private final int foundAmount;
    private final int targetAmount;

    private final CrownUser user;
    private final PlayerInventory inventory;
    private final Material material;

    public UserSellResult(int foundAmount, int targetAmount, CrownUser user, Material material) {
        this.foundAmount = foundAmount;
        this.targetAmount = targetAmount;
        this.user = user;
        this.inventory = user.getPlayer().getInventory();
        this.material = material;
    }

    /**
     * Creates an instance of the sell result where the user sold nothing
     * @param user The user that sold
     * @param target The target amount of items
     * @param lookedFor The material that was sold
     * @return The empty result instance
     */
    public static UserSellResult foundNone(CrownUser user, int target, Material lookedFor) {
        return new UserSellResult(0, target, user, lookedFor);
    }

    /**
     * Gets whether the result found anything to sell
     * @return Whether any items where found to sell
     */
    public boolean foundAnything() {
        return getFoundAmount() > 0;
    }

    /**
     * Gets the amount of items that were found
     * @return Found item amount
     */
    public int getFoundAmount() {
        return foundAmount;
    }

    /**
     * Gets the targeted amount of items
     * @return Target amount
     */
    public int getTargetAmount() {
        return targetAmount;
    }

    public CrownUser getUser() {
        return user;
    }

    public PlayerInventory getInventory() {
        return inventory;
    }

    public Material getMaterial() {
        return material;
    }

    /**
     * Gets whether the user sold everything in their inv of the given type
     * @return Whether they sold all items of the material in their inv.
     */
    public boolean sellingUnlimited() {
        return getTargetAmount() == -1;
    }
}
