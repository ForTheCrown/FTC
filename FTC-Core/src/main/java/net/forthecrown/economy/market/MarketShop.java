package net.forthecrown.economy.market;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.manager.UserManager;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.Struct;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

/**
 * A data class which carries data for a single shop
 */
public interface MarketShop extends JsonSerializable, Nameable, Struct {
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

    void setEvictionDate(@Nullable Date date);
    Date getEvictionDate();

    default boolean markedForEviction() {
        return getEvictionDate() != null;
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
