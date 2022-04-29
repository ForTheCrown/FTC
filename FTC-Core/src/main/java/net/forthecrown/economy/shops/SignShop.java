package net.forthecrown.economy.shops;

import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a sign shop lol
 * <p></p>
 * Implementation: {@link FtcSignShop}
 */
public interface SignShop extends InventoryHolder, Nameable {

    /**
     * Gets the file name of the shop
     * <p>
     * Example: world_101_63_1002
     * </p>
     * @return The file name of the shop
     */
    @Override
    default String getName() {
        return getFileName().toString();
    }

    void load();

    /**
     * Destroys the shop lol
     */
    void destroy(boolean removeBlock);

    /**
     * Gets the shop's location
     * @return The shop's location
     */
    WorldVec3i getPosition();

    /**
     * Gets the shops file name
     * @return The shop's file name
     */
    LocationFileName getFileName();

    /**
     * Gets the block the sign is at
     * @return The sign's block
     */
    Block getBlock();

    /**
     * Gets the shop's ownership
     * @return The shop's ownership
     */
    ShopOwnership getOwnership();

    /**
     * Gets the shops type
     * @return The shops type
     */
    ShopType getType();

    /**
     * Sets the shop's type
     * <p>Again, why is this here lol</p>
     * @param shopType
     */
    void setType(ShopType shopType);

    /**
     * Gets the price of the shop
     * @return The price of the shop
     */
    int getPrice();

    /**
     * Sets the price of the shop
     * @param price The new price of the shop
     */
    default void setPrice(int price) {
        setPrice(price, true);
    }

    /**
     * Sets the price of the shop
     * @param price The new price of the shop
     * @param updateSign whether the shop's sign should be updated
     */
    void setPrice(int price, boolean updateSign);

    /**
     * Gets the sign on which the shop is located
     * @return The S I G N
     */
    Sign getSign();

    /**
     * Updates the ingame sign representing this shop
     * <p>Should be used to apply changes to the sign</p>
     */
    void update();

    /**
     * Gets the shop's inventory
     * @return The shop's inventory
     */
    @Override
    @NotNull ShopInventory getInventory();

    /**
     * Gets the price for the given customer
     * @param customer The customer
     * @return The price that applies to that customer
     */
    int getPrice(ShopCustomer customer);

    /**
     * Gets the price display for the given customer
     * @param user The customer
     * @return The price display for the given customer
     */
    Component getPriceLineFor(CrownUser user);

    /**
     * Gets the shop's history
     * @return The shop's history
     */
    ShopHistory getHistory();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}