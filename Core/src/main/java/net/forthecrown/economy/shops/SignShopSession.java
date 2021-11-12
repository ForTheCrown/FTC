package net.forthecrown.economy.shops;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * A shop session.
 * <p></p>
 * A session is a the period of time where a user is buying from one store.
 * Like holding right click to mass buy from one shop or just buying once.
 *
 */
public class SignShopSession {
    private final Material material;
    private final SignShop shop;
    private final ShopType type;
    private final ShopInventory inventory;
    private final ShopOwnership ownership;

    private final ShopCustomer user;

    private int amount = 0;
    private Runnable onSessionExpire;

    public SignShopSession(SignShop shop, ShopCustomer user) {
        this.shop = shop;
        this.material = shop.getInventory().getExampleItem().getType();
        this.type = shop.getType();
        this.inventory = shop.getInventory();
        this.ownership = shop.getOwnership();

        this.user = user;
    }

    /**
     * Gets the inventory of the session's shop
     * @return This session's shop's inventory
     */
    public ShopInventory getShopInventory() {
        return inventory;
    }

    /**
     * Gets the current shop's exampleItem
     * @return Current shop's exampleItem
     */
    public ItemStack getExampleItem() {
        return getShopInventory().getExampleItem();
    }

    /**
     * Gets the type of the current shop
     * @return The current shop's type
     */
    public ShopType getType() {
        return type;
    }

    /**
     * Gets the material this shop trades
     * @return The current shop's material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Gets the price of the current shop
     * @return The current shop's price
     */
    public int getPrice() {
        return shop.getPrice();
    }

    /**
     * Gets the current shop
     * @return The current shop
     */
    public SignShop getShop() {
        return shop;
    }

    public ShopOwnership getOwnership() {
        return ownership;
    }

    /**
     * Gets whether the use has space for more items in their inventory.
     * @return Whether the user has inventory room.
     */
    public boolean customerHasSpace() {
        return getCustomerInventory().firstEmpty() != -1;
    }

    /**
     * Gets whether the shop has space for more items.
     * @return Whether the shop has inventory room.
     */
    public boolean shopHasSpace() {
        return !getShopInventory().isFull();
    }

    /**
     * Gets the session's user
     * @return The current user
     */
    public ShopCustomer getCustomer() {
        return user;
    }

    /**
     * Gets the inventory of the session's player
     * @return The player's inventory
     */
    public Inventory getCustomerInventory() {
        return user.getInventory();
    }

    /**
     * Gets the amount of items traded in the current session
     * @return The traded item amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Set the amount of items traded in the current session
     * @param amount The traded item amount
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Adds to the traded item amount
     * @param amount The amount to add
     */
    public void growAmount(int amount) {
        setAmount(getAmount() + amount);
    }

    /**
     * Gets the code to run when the session expires or ends
     * @return The code to run on this session's expiry or end
     */
    public Runnable getOnSessionExpire() {
        return onSessionExpire;
    }

    /**
     * Sets the code to run on the session's expiry or end.
     * @param onSessionExpire The code to run on session expiry
     */
    public void onSessionExpire(Runnable onSessionExpire) {
        this.onSessionExpire = onSessionExpire;
    }
}
