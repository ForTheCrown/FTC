package net.forthecrown.economy.market;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.click.ClickableTextNode;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.FtcVars;
import net.forthecrown.core.chat.TimePrinter;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserMarketData;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.utils.math.WorldVec3i;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.World;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * A central class for managing and handling actions between market shops
 */
public interface Markets extends CrownSerializer {

    /**
     * Gets a shop by the shop owner's UUID
     * @param owner The owner
     * @return The shop owned by that UUID, null if the UUID has no saved shop
     */
    MarketShop get(UUID owner);

    /**
     * Gets a shop by the name of it's WorldGuard region
     * @param claimName The shop's name
     * @return The shop by that name
     */
    MarketShop get(String claimName);

    MarketShop get(WorldVec3i pos);

    /**
     * Gets the world the markets are in
     * @return The market's world
     */
    World getWorld();

    /**
     * Add the given shop to this manager's control
     * @param claim The shop to add
     */
    void add(MarketShop claim);

    /**
     * Attempts to purchase the given shop
     * @param claim The shop to purchase
     * @param user The user purchasing
     * @throws CommandSyntaxException If the user cannot purchase this shop
     */
    void attemptPurchase(MarketShop claim, CrownUser user) throws CommandSyntaxException;

    /**
     * Claims this shop for the given user
     * @param shop The shop to claim
     * @param user The user claiming
     */
    void claim(MarketShop shop, CrownUser user) throws IllegalArgumentException;

    /**
     * unclaims the given shop
     * @param shop The shop to unclaim
     * @param complete Whether to also reset the shop
     */
    void unclaim(MarketShop shop, boolean complete) throws IllegalArgumentException;

    /**
     * Merges the given shop with the other shop
     * @param shop The first shop
     * @param merged The second shop
     */
    void merge(MarketShop shop, MarketShop merged) throws IllegalArgumentException;

    /**
     * Unmerges the given shop
     * @param shop The shop to unmerge
     */
    void unmerge(MarketShop shop) throws IllegalArgumentException;

    /**
     * Trusts the given UUID in the shop
     * @param shop the shop add trust in
     * @param uuid The uuid to trust
     */
    void trust(MarketShop shop, UUID uuid);

    WorldBounds3i getBounds(MarketShop shop);

    /**
     * Untrusts a user in the given shop
     * @param shop The shop to untrust in
     * @param uuid The UUID to untrust
     */
    void untrust(MarketShop shop, UUID uuid);

    /**
     * Removes an entrance from the shop at the given index
     * @param shop The shop to remove the entrance from
     * @param index The index of the entrance to remove
     */
    void removeEntrance(MarketShop shop, int index);

    /**
     * Adds an entrance to the given shop
     * @param shop The shop to add the entrance to
     * @param entrance The entrance to add
     */
    void addEntrance(MarketShop shop, ShopEntrance entrance);

    /**
     * Checks whether the shops are connected
     * @see MarketShop#getConnectedNames()
     * @param shop The first shop
     * @param other The second shop
     * @return True if connected, false if not
     */
    boolean areConnected(MarketShop shop, MarketShop other);

    /**
     * Connects the two shops
     * @param shop The first shop
     * @param other The second shop
     */
    void connect(MarketShop shop, MarketShop other);

    /**
     * Disconnects the two given shops
     * @param shop The first shop
     * @param other The second shop
     */
    void disconnect(MarketShop shop, MarketShop other);

    /**
     * Transfers a given shop to the given UUID
     * @param shop the shop to transfer
     * @param target The UUID to transfer to
     */
    void transfer(MarketShop shop, UUID target);

    default boolean isEmpty() {
        return getAllShops().isEmpty();
    }

    /**
     * Removes the given shop from from this market manager
     * @param shop The shop to remove
     */
    void remove(MarketShop shop);

    void resetFromBackup(MarketShop shop);

    void beginEviction(MarketShop shop, long evictTime, boolean automatic, Component reason);

    void stopEviction(MarketShop shop);

    /**
     * Clears all the shops from this manager
     */
    void clear();

    /**
     * Gets the amount of shops this manager has
     * @return The shops held by this manager
     */
    int size();

    /**
     * Gets all the UUIDs that currently own shops
     * @return All shop owners' UUIDs
     */
    Set<UUID> getOwners();

    /**
     * Gets the names of all the shops
     * @return The shops' names
     */
    Set<String> getNames();

    /**
     * Gets all the shops held by this manager
     * @return All this manager's shops
     */
    Collection<MarketShop> getAllShops();

    /**
     * Gets all shops with an owner
     * @return All owned shops
     */
    Collection<MarketShop> getOwnedShops();

    /**
     * Checks if the given ownership can change status and throws an exception
     * if they cant
     * <p></p>
     * Ye idk lol, didn't know where else to put stuff this
     * @param ownership The market ownership to check
     * @throws CommandSyntaxException If the given owner can't change status
     */
    static void checkStatusChange(UserMarketData ownership) throws CommandSyntaxException {
        checkStatusChange(ownership,"market.cannot.changeStatus");
    }

    static void checkCanPurchase(UserMarketData ownership) throws CommandSyntaxException {
        checkStatusChange(ownership,"market.cannot.purchase");
    }

    static void checkStatusChange(UserMarketData ownership, String transKey) throws CommandSyntaxException {
        if(ownership.canChangeStatus()) return;

        long nextAllowed = ownership.getLastStatusChange() + FtcVars.marketStatusCooldown.get();
        long remaining = nextAllowed - System.currentTimeMillis();

        throw FtcExceptionProvider.translatable(transKey, new TimePrinter(remaining).print());
    }

    Book getPurchaseBook(MarketShop shop, CrownUser user, ClickableTextNode node);

    /**
     * Refreshes a shop
     * <p></p>
     * Aka, makes sure all the entrances exist
     * @param shop The shop to refresh
     */
    void refresh(MarketShop shop);
}