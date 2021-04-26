package net.forthecrown.core.api;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.enums.ShopType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a sign shop lol
 */
public interface SignShop extends CrownSerializer<FtcCore>, InventoryHolder, Nameable {

    /**
     * Gets the file name of the shop
     * <p>
     * Example: world_101_63_1002
     * </p>
     * @return The file name of the shop
     */
    @Override
    String getName();

    /**
     * Destroys the shop lol
     */
    void destroy(boolean removeBlock);

    /**
     * Unloads the shop
     */
    void unload();

    /**
     * Gets the hopper inventory with 1 available slot, used for setting the exampleItem of a shop
     * @return the example inventory
     */
    Inventory getExampleInventory();

    /**
     * Gets the shop's location
     * @return The shop's location
     */
    Location getLocation();

    /**
     * Gets the block the sign is at
     * @return The sign's block
     */
    Block getBlock();

    /**
     * Gets the owner of the shop
     * @return the UUID of the owner of the shop
     */
    UUID getOwner();

    /**
     * Sets the owner of a shop
     * <p>Why does this exist lol</p>
     * @param shopOwner the new owner
     */
    void setOwner(UUID shopOwner);

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
    Integer getPrice();

    /**
     * Sets the price of the shop
     * @param price The new price of the shop
     */
    void setPrice(Integer price);

    /**
     * Sets the price of the shop
     * @param price The new price of the shop
     * @param updateSign whether the shop's sign should be updated
     */
    void setPrice(Integer price, boolean updateSign);

    /**
     * Gets if the shop is out of stock or not
     * @return Whether the shop is out of stock or not... duh
     */
    boolean isOutOfStock();

    /**
     * Sets if the shop is out of stock
     * @param outOfStock Whether the shop is out of stock
     */
    void setOutOfStock(boolean outOfStock);

    /**
     * Gets if the file was deleted
     * <p>Deleted in this context means that the sign the shop is based on has been broken by a player... I don't wanna know what happens when a non player breaks a sign</p>
     * <p>This is used basically only in the save method to stop it from saving the file when it's supposed to have been deleted</p>
     * @return Whether the shop has been deleted or not
     */
    boolean wasDeleted();

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

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}
