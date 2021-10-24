package net.forthecrown.economy.shops;

import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

    private final CrownUser user;
    private final Player player;
    private final PlayerInventory playerInventory;

    private final CrownUser owner;

    private int amount = 0;
    private Runnable onSessionExpire;

    public SignShopSession(SignShop shop, CrownUser user) {
        this.shop = shop;
        this.material = shop.getInventory().getExampleItem().getType();
        this.type = shop.getType();
        this.inventory = shop.getInventory();

        this.user = user;
        this.player = user.getPlayer();
        this.playerInventory = player.getInventory();

        this.owner = UserManager.getUser(shop.getOwner());
    }

    /**
     * Gets the owner of the shop the user is interacting with.
     * <p></p>
     * Since admin shops don't handle owners differently, this works in all cases
     *
     * @return The shop's owner
     */
    public CrownUser getOwner() {
        return owner;
    }

    /**
     * Gets the inventory of the session's shop
     * @return This session's shop's inventory
     */
    public ShopInventory getInventory() {
        return inventory;
    }

    /**
     * Gets the current shop's exampleItem
     * @return Current shop's exampleItem
     */
    public ItemStack getExampleItem() {
        return getInventory().getExampleItem();
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

    /**
     * Gets whether the use has space for more items in their inventory.
     * @return Whether the user has inventory room.
     */
    public boolean userHasSpace() {
        return getPlayerInventory().firstEmpty() != -1;
    }

    /**
     * Gets whether the shop has space for more items.
     * @return Whether the shop has inventory room.
     */
    public boolean shopHasSpace() {
        return !getInventory().isFull();
    }

    /**
     * Gets the session's user
     * @return The current user
     */
    public CrownUser getCustomer() {
        return user;
    }

    /**
     * Gets the session's player
     * @return The current player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the inventory of the session's player
     * @return The player's inventory
     */
    public PlayerInventory getPlayerInventory() {
        return playerInventory;
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
