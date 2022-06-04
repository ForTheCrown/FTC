package net.forthecrown.economy.shops;

import net.forthecrown.core.Crown;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.LocationFileName;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Manages shops n stuff
 * <p></p>
 * Implementation: {@link FtcShopManager}
 */
public interface ShopManager {

    /**
     * Checks whether a block is a preexisting signshop.
     * A null check is also performed in the statement
     * @param block The block to check
     * @return Whether the block is a shop or not
     */

    static boolean isShop(Block block) {
        if(block == null) return false;

        if (block.getState() instanceof Sign sign) {
            PersistentDataContainer container = sign.getPersistentDataContainer();
            return container.has(ShopConstants.SHOP_KEY, PersistentDataType.TAG_CONTAINER);
        } else {
            return false;
        }
    }

    static boolean mayEdit(SignShop shop, UUID uuid) {
        if (shop == null) return false;
        if (shop.getOwnership().mayEditShop(uuid)) return true;

        WorldVec3i vec = shop.getPosition();
        MarketShop s = Crown.getMarkets().get(vec);

        if (s == null || !s.hasOwner()) {
            return false;
        }

        if (!s.isMemberEditingAllowed()) {
            return false;
        }

        return s.getCoOwners().contains(uuid);
    }

    /**
     * Tells a shops owner of their shop's stock being 'bad'
     * <p></p>
     * Bad means different things basesd on shop type:
     * <p></p>
     * For sell shops it means the stock is full and needs emptying
     * <p></p>
     * For buy shops it means the shop is out of stock and needs refilling
     * @param owner The shop's owner
     * @param shop The shop to inform of
     */
    static void informOfStockIssue(CrownUser owner, SignShop shop){
        if(shop.getType().isAdmin()) return;

        //If no good, then no go
        if ((shop.getType() != ShopType.BUY || !shop.getInventory().isEmpty()) && (shop.getType() != ShopType.SELL || !shop.getInventory().isFull())) {
            return;
        }

        Location l = shop.getPosition().toLocation();
        Component specification = Component.translatable("shops." + (shop.getType().isBuyType() ? "out" : "full"));
        Component builder = Component.translatable("shops.stockWarning",
                NamedTextColor.YELLOW,
                FtcFormatter.prettyLocationMessage(l, false),
                specification
        );

        owner.sendMessage(builder);
    }

    /**
     * Gets a shop at the given location
     * @param signShop The location of the sign
     * @return The shop at the location, null if no shop exists at the given location
     */
    default SignShop getShop(Location signShop) {
        return getShop(WorldVec3i.of(signShop));
    }

    /**
     * Gets a shop at the given location
     * @param vec The location of the sign
     * @return The shop at the location, null if no shop exists at the given location
     */
    SignShop getShop(WorldVec3i vec);

    /**
     * Gets a shop from a given name
     * @param name The shop's name
     * @return The shop with the given name
     */
    default SignShop getShop(String name) {
        return getShop(LocationFileName.parse(name));
    }

    /**
     * Gets a shop from a given name
     * @param name The shop's name
     * @return The shop with the given name
     */
    default SignShop getShop(LocationFileName name) {
        return getShop(name.toVector());
    }

    /**
     * Creates a sign shop at the given location
     * @param location The shop's location
     * @param shopType The shop's type
     * @param price The shop's starting price
     * @param ownerUUID The UUID of the owner
     * @return The created shop
     */
    default SignShop createSignShop(Location location, ShopType shopType, int price, UUID ownerUUID) {
        return createSignShop(WorldVec3i.of(location), shopType, price, ownerUUID);
    }

    /**
     * Creates a sign shop at the given location
     * @param vec The shop's location
     * @param type The shop's type
     * @param price The shop's starting price
     * @param owner The UUID of the owner
     * @return The created shop
     */
    SignShop createSignShop(WorldVec3i vec, ShopType type, int price, UUID owner);

    /**
     * Gets the "Price: 12345 Rhines" line for the shop's sign with the given amount.
     * @param amount The amount to get the text for
     * @return The created text
     */
    Component getPriceLine(int amount);

    /**
     * Gets the hopper inventory with 1 available slot, used for setting the exampleItem of a shop
     * @return the example inventory
     */
    Inventory getExampleInventory();

    File shopListFile();

    /**
     * Saves all shops and the shop list
     */
    void save();

    /**
     * Reloads all shops and the shop list
     */
    void reload();

    /**
     * Clears all loaded shops
     */
    void clearLoaded();

    void onShopDestroy(SignShop shop);

    /**
     * Gets the interaction handler for shops
     * @return The shop interaction handler
     */
    ShopInteractionHandler getInteractionHandler();

    Collection<SignShop> getLoadedShops();

    /**
     * Loads all sign shops async
     * @return All sign shops that exist
     */
    CompletableFuture<List<SignShop>> getAllShops();
}