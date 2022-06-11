package net.forthecrown.economy.market;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.economy.Taxable;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.Struct;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Date;
import java.util.UUID;

/**
 * A data class which carries data for a single shop
 */
public interface MarketShop extends JsonSerializable, Nameable, Struct, Taxable {
    /**
     * Gets the worldguard region linked to this shop
     * @return The shop's worldguard region
     */
    ProtectedRegion getWorldGuard();

    /**
     * Gets the name of the worldguard region linked to this shop
     * @return The shop's worldguard region's name
     */
    @Override
    default String getName() {
        return getWorldGuard().getId();
    }

    default Component wgDisplayName() {
        return Component.text('[' + getName() + ']')
                .color(NamedTextColor.AQUA)
                .hoverEvent(Component.text("Click for info"))
                .clickEvent(ClickEvent.runCommand("/rg info " + getName()));
    }

    /**
     * Gets all the entrances to the shop
     * @return Mutable list of entrances to this shop
     */
    ObjectList<ShopEntrance> getEntrances();

    /**
     * Gets the price of the shop used by people
     * looking to purchase it
     * @return The shop's price
     */
    int getPrice();

    /**
     * Sets the shop's price
     * @param price The new price
     */
    void setPrice(int price);

    /**
     * Gets the date at which this shop was purchased
     * @return The date at which this shop was purchased, null if no current owner
     */
    Date getDateOfPurchase();

    /**
     * Sets the date of purchase of this shop
     * @param dateOfPurchase The new date of purchase
     */
    void setDateOfPurchase(Date dateOfPurchase);

    /**
     * Checks whether the owner can be evicted
     * @return Whether the owner can be evicted, will return false if the shop has no owner
     */
    boolean canBeEvicted();

    /**
     * Gets the shop this shop is merged with
     * @return The shop this shop is merged with, null if not merged
     */
    MarketShop getMerged();

    /**
     * Sets the shop this shop is merged with
     * @param shop The merged shop
     */
    void setMerged(MarketShop shop);

    /**
     * Checks whether the shop is merged
     * @return gerMerged() != null
     */
    default boolean isMerged() {
        return getMerged() != null;
    }

    /**
     * Gets the current shop owner
     * @return The current shop owner, null if not owned
     */
    UUID getOwner();

    /**
     * Sets the current owner
     * @param uuid The owner
     */
    void setOwner(UUID uuid);

    /**
     * Checks if the shop has an owner
     * @return getOwner() != null;
     */
    default boolean hasOwner() {
        return getOwner() != null;
    }

    /**
     * Gets the user object of the owner
     * @return The owner's user object, or null, if not owned
     */
    default CrownUser ownerUser() {
        return hasOwner() ? UserManager.getUser(getOwner()) : null;
    }

    /**
     * Gets all trusted co owners of this shop
     * @return The trusted co owners of the shop
     */
    ObjectList<UUID> getCoOwners();

    /**
     * Gets the names of all the shops this shop is connected to.
     * <p></p>
     * Note: "Connected" is up to interperitation, as connected shops
     * are set manually and not denoted by an actual physical connection
     * between the shops
     * <p></p>
     * Connected shops are the shops this shop is allowed to merge with
     * @return
     */
    ObjectList<String> getConnectedNames();

    /**
     * Gets the past scans of this market
     * @return Previous market scans
     */
    ObjectList<MarketScan> getScans();

    /**
     * Checks if this market should run a scan
     * @return True, if a new scan should be run on this market
     */
    default boolean shouldRunScan() {
        if (getScans().isEmpty()) {
            long since = TimeUtil.timeSince(getDateOfPurchase().getTime());
            return since >= MarketScan.SCAN_INTERVAL.get();
        }

        MarketScan s = getScans().get(0);

        return TimeUtil.hasCooldownEnded(MarketScan.SCAN_INTERVAL.get(), s.getScanTime());
    }

    /**
     * Sets the eviction data
     * @param data The new eviction data, null, to cancel eviction
     */
    void setEviction(MarketEviction data);

    /**
     * Gets the current eviction data
     * @return Current eviction data, null, if the shop isn't being evicted
     */
    MarketEviction getEviction();

    /**
     * Sets if members are allowed to edit shops
     * @param b If member editing is allowed
     */
    void setMemberEditingAllowed(boolean b);

    /**
     * Gets if co-owners of this shop are allowed to edit
     * each other's shops
     * @return If sign shops' editing by co-owners is allowed
     */
    boolean isMemberEditingAllowed();

    /**
     * Checks if this market is marked for eviction
     * @return True, if the market is marked for eviction
     */
    default boolean markedForEviction() {
        return getEviction() != null;
    }

    default Vector3i getBackupPos() {
        return Vector3i.of(getWorldGuard().getMinimumPoint())
                .subtract(0, 30, 0);
    }

    default Vector3i getMin() {
        return Vector3i.of(getWorldGuard().getMinimumPoint());
    }

    default Vector3i getMax() {
        return Vector3i.of(getWorldGuard().getMaximumPoint());
    }

    default Vector3i getSize() {
        return getMax().subtract(getMin());
    }
}