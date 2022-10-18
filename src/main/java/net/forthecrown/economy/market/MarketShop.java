package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDiscord;
import net.forthecrown.core.Vars;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.user.data.MailMessage;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserMarketData;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.math.Vectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.bukkit.World;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector3i;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static net.forthecrown.core.FtcDiscord.C_MARKETS;
import static net.forthecrown.economy.market.MarketEviction.SOURCE_AUTOMATIC;
import static net.forthecrown.economy.market.MarketEviction.SOURCE_UNKNOWN;
import static net.forthecrown.user.data.UserTimeTracker.UNSET;

/**
 * A market shop represents a single player's shop
 * in the spawn region.
 * <p>
 * This system mostly functions by tying these markets
 * to WorldGuard regions and syncing the market's data
 * to the region.
 */
@RequiredArgsConstructor
public class MarketShop {
    /* ----------------------------- CONSTANTS ------------------------------ */

    public static final int UNSET_PRICE = -1;

    /**
     * The maximum number of scans a shop can have before
     * it starts testing to see if the members should be
     * evicted.
     */
    public static final int MAX_SCANS = 3;

    // JSON Keys
    public static final String
            KEY_NAME = "name",
            KEY_PRICE = "price",
            KEY_ENTRANCES = "entrances",
            KEY_CONNECTED = "connected",
            KEY_OWNER = "owner",
            KEY_PURCHASE_DATE = "purchaseDate",
            KEY_RESET = "resetData",
            KEY_SCANS = "scans",
            KEY_EVICTION = "eviction",
            KEY_MERGED = "merged",
            KEY_MEMBERS = "members",
            KEY_EDITING = "memberEditingAllowed",
            KEY_CURRENT_OWNER = "currentOwner";

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /** World guard region of this shop */
    @Getter
    private final ProtectedRegion worldGuard;

    /** Owner of this market shop */
    @Getter @Setter
    private UUID owner;

    /**
     * The time stamp of when this shop was purchased, or
     * {@link net.forthecrown.user.data.UserTimeTracker#UNSET},
     * if the shop currently has no owner
     */
    @Getter @Setter
    private long purchaseDate;

    /** the UUIDs of every player that is a co owner in this shop */
    @Getter
    private final Set<UUID> members = new ObjectOpenHashSet<>();

    /** Every entrance to the shop */
    @Getter
    private final List<ShopEntrance> entrances = new ObjectArrayList<>();

    /** The names of every shop connected to this one */
    @Getter
    private final Set<String> connected = new ObjectOpenHashSet<>();

    /** True, if members are allowed to edit each-others' shops, false otherwise */
    @Getter @Setter
    private boolean memberEditingAllowed = true;

    /** Market's current eviction data, will be null if no eviction in progress */
    @Getter
    private MarketEviction eviction;

    /**
     * The market's price, {@link #UNSET_PRICE} if unset, calling {@link #getPrice()}
     * when the price is unset will return {@link Vars#defaultMarketPrice}
     */
    private int price = UNSET_PRICE;

    /** The name of the shop this shop is merged with */
    private String mergedName;

    @Getter @Setter
    private MarketReset reset;

    private final List<MarketScan> scans = new ObjectArrayList<>();

    /* ----------------------------- ACCESSORS ------------------------------ */

    /**
     * Gets the name of the worldguard region
     * this shop is linked to
     * @return The underlying worldguard region
     */
    public String getName() {
        return worldGuard.getId();
    }

    /**
     * Gets the display name of the worldguard region
     * @return The world guard region's display name
     */
    public Component wgDisplayName() {
        return Component.text('[' + getName() + ']')
                .color(NamedTextColor.AQUA)
                .hoverEvent(Component.text("Click for info"))
                .clickEvent(ClickEvent.runCommand("/rg -w \"world\" info " + getName()));
    }

    /**
     * Tests if this shop has been marked for eviction
     * @return True, if {@link #getEviction()} is not null, false otherwise
     */
    public boolean markedForEviction() {
        return eviction != null;
    }

    public boolean hasOwner() {
        return getOwner() != null;
    }

    /**
     * Checks whether the shop is merged
     * @return gerMerged() != null
     */
    public boolean isMerged() {
        return getMerged() != null;
    }

    /**
     * Gets the user object of the owner
     * @return The owner's user object, or null, if not owned
     */
    public User ownerUser() {
        return hasOwner() ? Users.get(getOwner()) : null;
    }

    /**
     * Gets this shop's price, if this market's
     * {@link #price} field is equal to {@link #UNSET_PRICE}
     * then this method will return {@link Vars#defaultMarketPrice}
     * instead.
     *
     * @return The shop's effective price
     */
    public int getPrice() {
        return price == UNSET_PRICE ? Vars.defaultMarketPrice : price;
    }

    /** Sets the shop's price */
    public void setPrice(int price) {
        this.price = Mth.clamp(price, UNSET_PRICE, Vars.maxMoneyAmount);
    }

    /**
     * Sets the shop's eviction data
     * <p>
     * If there is an already ongoing eviction,
     * it will be cancelled and the given eviction
     * will be set and started in its stead.
     *
     * @param eviction The eviction data to set
     */
    public void setEviction(MarketEviction eviction) {
        if (this.eviction != null) {
            this.eviction.cancel();
        }

        this.eviction = eviction;

        if (eviction != null) {
            eviction.start();
        }
    }

    /* ----------------------------- CLAIMING AND UNCLAIMING ------------------------------ */

    /**
     * Resets the shop using the data in {@link #getReset()}
     * @return True, if shop was successfully reset, false
     *         if {@link #reset} == null
     */
    public boolean reset() {
        //Figure out positions for pasting
        var markets = Crown.getEconomy().getMarkets();

        if (reset == null) {
            return false;
        }

        reset.place(markets.getWorld());
        return true;
    }

    /**
     * Attempts to purchase this shop
     * @param user The user purchasing
     * @throws CommandSyntaxException If the user cannot purchase this shop
     */
    public void attemptPurchase(User user) throws CommandSyntaxException {
        UserMarketData ownership = user.getMarketData();

        //If they already own a shop
        if (MarketManager.ownsShop(user)) {
            throw Exceptions.MARKET_ALREADY_OWNER;
        }

        //If the shop already has an owner, idk how this could even be triggered lol
        if (hasOwner()) {
            throw Exceptions.MARKET_ALREADY_OWNED;
        }

        //Check if they can even buy it
        MarketManager.checkCanPurchase(ownership);
        int price = getPrice();

        //Check if they can afford it
        if (!user.hasBalance(price)) {
            throw Exceptions.cannotAfford(price);
        }

        user.removeBalance(price);

        //Claim it
        user.sendMessage(Messages.MARKET_BOUGHT);
        claim(user);
    }

    /**
     * Claims this shop for the given user
     * <p>
     * This method will update user's {@link TimeField#MARKET_LAST_ACTION}
     * and {@link TimeField#MARKET_OWNERSHIP_STARTED} fields. This will
     * also make a copy of the shop 40 blocks under the market itself
     * and then set that to be the shop's {@link #reset}
     * <p>
     * This also updates the shop's entrances and world guard region
     *
     * @param user The user claiming the shop
     * @throws IllegalArgumentException If the shop already has an owner
     */
    public void claim(User user) throws IllegalArgumentException {
        Validate.isTrue(!hasOwner(), "Market already has owner");

        var markets = Crown.getEconomy().getMarkets();
        var world = markets.getWorld();

        var timeTracker = user.getTimeTracker();
        timeTracker.setCurrent(TimeField.MARKET_LAST_ACTION);

        if (!timeTracker.isSet(TimeField.MARKET_OWNERSHIP_STARTED)) {
            timeTracker.setCurrent(TimeField.MARKET_OWNERSHIP_STARTED);
        }

        setOwner(user.getUniqueId());
        setPurchaseDate(System.currentTimeMillis());

        worldGuard.getMembers().addPlayer(user.getUniqueId());

        markets.onShopClaim(this);

        for (ShopEntrance e: entrances) {
            e.onClaim(user, world);
        }

        Vector3i wgMin = Vectors.from(worldGuard.getMinimumPoint());
        Vector3i wgMax = Vectors.from(worldGuard.getMaximumPoint());

        MarketReset reset = new MarketReset(
                wgMin,
                wgMin.sub(0, 40, 0),
                wgMax.sub(wgMin)
        );

        reset.copy(world);
        setReset(reset);
    }

    /**
     * Unclaims the shop and updates the worldguard region,
     * shop entrances and user time field as well as clearing
     * the scans list and co-owners list.
     * <p>
     * If <code>complete == true</code>, then the shop is also
     * reset and the owner's {@link TimeField#MARKET_OWNERSHIP_STARTED}
     * field is removed.
     *
     * @param complete True to remove the user's {@link TimeField#MARKET_OWNERSHIP_STARTED}
     *                 field and to reset the shop.
     *
     * @throws IllegalArgumentException If the shop has no owner
     */
    public void unclaim(boolean complete) throws IllegalArgumentException {
        Validate.isTrue(hasOwner(), "Market has no owner");
        var world = Crown.getEconomy().getMarkets().getWorld();

        if (isMerged()) {
            unmerge();
        }

        User owner = ownerUser();
        owner.setTimeToNow(TimeField.MARKET_LAST_ACTION);

        Crown.getEconomy().getMarkets().onShopUnclaim(this);

        setPurchaseDate(UNSET);
        setOwner(null);
        setEviction(null);

        worldGuard.getMembers().clear();
        members.clear();
        scans.clear();

        if (complete) {
            owner.getTimeTracker().remove(TimeField.MARKET_OWNERSHIP_STARTED);
            reset();
        }

        for (ShopEntrance e: entrances) {
            e.onUnclaim(world, this);
        }
    }

    /**
     * Transfers this shop to the given target.
     * This used to be a separate method for a reason,
     * however it nows just calls {@link #unclaim(boolean)} with
     * the boolean parameter as false, and then calls
     * {@link #claim(User)} for the given user.
     *
     * @param target The user to transfer the shop to
     * @throws IllegalArgumentException If the shop has no owner
     */
    public void transfer(User target) throws IllegalArgumentException {
        unclaim(false);
        claim(target);
    }

    /* ----------------------------- MERGING ------------------------------ */

    /**
     * Merges this shop with the given shop and syncs the
     * world guard data of both shops
     *
     * @param other The shop to merge with
     *
     * @throws IllegalArgumentException Thrown either if the shop is already
     *                                  merged, or if the given shop is this
     *                                  shop
     */
    public void merge(MarketShop other) throws IllegalArgumentException {
        Validate.isTrue(!isMerged(), "Shop is already merged");
        Validate.isTrue(!equals(other), "Same shops given in parameters");

        setMerged(other);
        other.setMerged(this);

        other.syncWorldGuard();
        this.syncWorldGuard();
    }

    /**
     * Unmerges the shop and syncs the data of both
     * shops to their world guard regions
     *
     * @throws IllegalArgumentException If the shop is not merged
     */
    public void unmerge() throws IllegalArgumentException {
        Validate.isTrue(isMerged(), "Given shop was not merged");

        MarketShop merged = getMerged();

        merged.setMerged(null);
        setMerged(null);

        syncWorldGuard();
        merged.syncWorldGuard();
    }

    /**
     * Gets the shop this shop is 'merged' with
     * @return This shop's merged shop, or null, if not merged
     */
    public MarketShop getMerged() {
        return Util.isNullOrBlank(mergedName) ? null : Crown.getEconomy().getMarkets().get(mergedName);
    }

    /**
     * Sets the shop this shop is merged with
     * <p>
     * Be aware, this is just a setter for a single
     * value, if you're looking for a setter to keep
     * data between shops synced, use {@link #merge(MarketShop)}
     * and {@link #unmerge()}
     *
     * @param shop The shop to set, null, to unmerge
     */
    public void setMerged(MarketShop shop) {
        mergedName = shop == null ? null : shop.getName();
    }

    /* ----------------------------- MEMBERSHIPS ------------------------------ */

    /**
     * Adds the given player's UUID to this shop's
     * members list
     * @param uuid The ID to add to the members list
     * @throws IllegalArgumentException If the shop has no owner
     */
    public void trust(UUID uuid) throws IllegalArgumentException {
        Validate.isTrue(hasOwner(), "Market has no owner");

        members.add(uuid);
        syncWorldGuard();
    }

    /**
     * Removes the given player's UUID from this shop's
     * members list
     * @param uuid The ID to remove
     * @throws IllegalArgumentException If the shop has no owner
     */
    public void untrust(UUID uuid) throws IllegalArgumentException {
        Validate.isTrue(hasOwner(), "Market has no owner");

        members.remove(uuid);
        syncWorldGuard();
    }

    /* ----------------------------- CONNECTIONS ------------------------------ */

    /**
     * Connects this shop to the given shop
     * @param other The shop to connect
     */
    public void connect(MarketShop other) {
        connected.add(other.getName());
        other.connected.add(getName());
    }

    /**
     * Disconnects this shop from the given shop
     * @param shop The shop to disconnect from
     */
    public void disconnect(MarketShop shop) {
        connected.remove(shop.getName());
        shop.connected.remove(getName());
    }

    public boolean isConnected(MarketShop other) {
        return other.connected.contains(getName())
                && connected.contains(other.getName());
    }

    /* ----------------------------- ENTRANCES ------------------------------ */

    public void addEntrance(ShopEntrance entrance) {
        entrances.add(entrance);
    }

    public void removeEntrance(int index) {
        var entrance = entrances.remove(index);

        var world = Crown.getEconomy().getMarkets().getWorld();
        entrance.removeSign(world);
        entrance.removeNotice(world);
    }

    /* ----------------------------- EVICTION ------------------------------ */

    /**
     * Begins an eviction of this shop.
     * <p>
     * This will alert the user their shop has been marked
     * for eviction with either a message and/or sending them
     * a mail, depending on if they're online.
     * <p>
     * Otherwise, it just creates an {@link MarketEviction} instance
     * and calls {@link #setEviction(MarketEviction)}.
     *
     * @param evictionDate The timestamp of when the eviction will occur
     * @param reason The reason for the eviction
     *
     * @param source The text name of the entity issuing the eviction, will
     *               either be the staff member's playername of
     *               {@link MarketEviction#SOURCE_AUTOMATIC} if it's an
     *               automatic eviction.
     *
     * @throws IllegalArgumentException If the shop has no owner or is already marked
     *                                  for eviction
     */
    public void beginEviction(long evictionDate, Component reason, String source)
            throws IllegalArgumentException
    {
        Validate.isTrue(hasOwner(), "Cannot evict shop with no owner");
        Validate.isTrue(!markedForEviction(), "Shop '%s' is already marked for eviction", getName());

        MarketEviction data = new MarketEviction(
                this,
                evictionDate, reason,
                source
        );

        setEviction(data);

        User user = ownerUser();
        MailMessage m = MailMessage.of(Messages.evictionMail(data));

        if (user.isOnline()) {
            user.sendMessage(Messages.evictionNotice(data));
            m.setRead(true);
        }

        FtcDiscord.staffLog(C_MARKETS, "{}, owner `{}`, has been marked for eviction, reason: `{}`, source: `{}`",
                getName(), user.getNickOrName(), Text.plain(reason), source
        );

        user.getMail().add(m);
    }

    /**
     * Stops this shop's eviction.
     * <p>
     * Will also inform the user that their shop eviction
     * has been cancelled.
     *
     * @throws IllegalArgumentException If the shop has no owner or is NOT marked for eviction
     */
    public void stopEviction() throws IllegalArgumentException {
        Validate.isTrue(hasOwner(), "Shop has no owner");
        Validate.isTrue(markedForEviction(), "Shop '%s' is not marked for eviction", getName());

        setEviction(null);

        User user = ownerUser();

        var message = Messages.EVICTION_CANCELLED;
        user.sendMessage(message);
        user.getMail().add(message);

        FtcDiscord.staffLog(C_MARKETS, "{}, owner '{}', eviction cancelled",
                getName(), user.getNickOrName()
        );
    }

    /**
     * Refreshes a shop
     * <p>
     * Aka, makes sure all the entrances exist
     */
    public void refresh(World marketWorld) {
        for (ShopEntrance e: entrances) {
            if (hasOwner()) {
                e.onClaim(ownerUser(), marketWorld);
            } else {
                e.onUnclaim(marketWorld, this);
            }
        }
    }

    /**
     * Called by {@link MarketManager#onDayChange(ZonedDateTime)}
     * to test if the market should start the automatic
     * eviction process.
     * <p>
     * This will first test if the user has been online in
     * the past {@link Vars#markets_maxOfflineTime} time, if
     * not, then it begins the eviction with the
     * {@link Messages#MARKET_EVICT_INACTIVE} message as the
     * reason for the eviction.
     * <p>
     * If the aforementioned test passes, then this will scan
     * the shop's signshops, if there is not enough of them,
     * or if the rate of stocked shop to unstocked shop is
     * higher than {@link Vars#markets_minStockRequired}, then
     * this begins the automatic eviction.
     *
     * @param markets The markets manager
     */
    public void validateOwnership(MarketManager markets) {
        if (!hasOwner()) {
            return;
        }

        var owner = ownerUser();
        owner.unloadIfOffline();

        var lastOnline = owner.getTime(TimeField.LAST_LOGIN);

        // If owner has been offline for a long time
        if (lastOnline != UNSET
                && Time.isPast(lastOnline + Vars.markets_maxOfflineTime)
        ) {
            beginEviction(
                    System.currentTimeMillis() + Vars.markets_evictionDelay,
                    Messages.MARKET_EVICT_INACTIVE,
                    SOURCE_AUTOMATIC
            );

            return;
        }

        MarketScan scan = MarketScan.create(markets.getWorld(), this);
        scans.add(0, scan);

        // There's got to be at least MAX_SCANS weeks worth of
        // scans for us to properly assess the shop's
        // state
        if (scans.size() > MAX_SCANS) {
            scans.subList(MAX_SCANS, scans.size()).clear();
        } else {
            return;
        }

        // Tally up Scans
        int failedAmount = 0;
        int failedStock = 0;

        for (var s: scans) {
            int totalShops = s.stockedCount() + s.unstockedCount();
            double requiredStock = totalShops * GenericMath.clamp(Vars.markets_minStockRequired, 0, 1);

            // If there is enough shops and if enough are in stock
            // skip this scan
            if (totalShops >= Vars.markets_minShopAmount && s.stockedCount() >= requiredStock) {
                return;
            }

            if (requiredStock > s.stockedCount()) {
                failedStock++;
                continue;
            }

            if (totalShops < Vars.markets_minShopAmount) {
                failedAmount++;
            }
        }

        Component reason = failedAmount < failedStock ?
                Messages.MARKET_EVICT_STOCK
                : Messages.tooLittleShops();

        beginEviction(System.currentTimeMillis() + Vars.markets_evictionDelay, reason, SOURCE_AUTOMATIC);
    }

    /**
     * Syncs this shop's members and owner to the world guard,
     * factoring in the shop's merged shop as well.
     */
    public void syncWorldGuard() {
        var members = worldGuard.getMembers();
        members.clear();

        if (!hasOwner()) {
            return;
        }

        var merged = getMerged();

        forEachMember(uuid -> {
            members.addPlayer(uuid);

            if (merged != null) {
                merged.getWorldGuard().getMembers().addPlayer(uuid);
            }
        });

        if (merged != null) {
            merged.forEachMember(members::addPlayer);
        }
    }

    /**
     * Iterates through each member of the shop including
     * the shop's owner
     * @param consumer The consumer to apply to members
     */
    public void forEachMember(Consumer<UUID> consumer) {
        if (!hasOwner()) {
            return;
        }

        consumer.accept(owner);
        members.forEach(consumer);
    }
    
    /* ----------------------------- SERIALIZATION ------------------------------ */

    public void serialize(JsonWrapper json) {
        json.add(KEY_PRICE, price);

        if (hasOwner()) {
            JsonWrapper ownership = JsonWrapper.create();

            ownership.addUUID(KEY_OWNER, owner);

            if (purchaseDate != UNSET) {
                ownership.addTimeStamp(KEY_PURCHASE_DATE, purchaseDate);
            }

            if (markedForEviction()) {
                ownership.add(KEY_EVICTION, eviction);
            }

            if (mergedName != null) {
                ownership.add(KEY_MERGED, mergedName);
            }

            if (!members.isEmpty()) {
                ownership.addList(KEY_MEMBERS, members, JsonUtils::writeUUID);
            }

            if (!memberEditingAllowed) {
                ownership.add(KEY_EDITING, false);
            }

            json.add(KEY_CURRENT_OWNER, ownership);
        }

        if (!entrances.isEmpty()) {
            json.addList(KEY_ENTRANCES, entrances);
        }

        if (!connected.isEmpty()) {
            json.addList(KEY_CONNECTED, connected, JsonPrimitive::new);
        }

        if (!scans.isEmpty()) {
            json.addList(KEY_SCANS, scans, MarketScan::serialize);
            scans.sort(MarketScan.COMPARATOR);
        }
        
        if (reset != null) {
            json.add(KEY_RESET, reset.serialize());
        }
    }

    public void deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
        price = json.getInt(KEY_PRICE);

        if(json.has(KEY_CURRENT_OWNER)) {
            JsonWrapper ownership = json.getWrapped(KEY_CURRENT_OWNER);

            owner = ownership.getUUID(KEY_OWNER);
            purchaseDate = ownership.getTimeStamp(KEY_PURCHASE_DATE, UNSET);

            members.addAll(ownership.getList(KEY_MEMBERS, JsonUtils::readUUID));

            if(ownership.has("merged")) {
                mergedName = ownership.getString(KEY_MERGED);
            }

            memberEditingAllowed = ownership.getBool(KEY_EDITING, true);

            if(ownership.has("evictionDate")) {
                MarketEviction data = new MarketEviction(
                        this,
                        ownership.getDate("evictionData").getTime(),
                        Component.text("Admin"),
                        SOURCE_UNKNOWN
                );

                setEviction(data);
            } else if (ownership.has(KEY_EVICTION)) {
                setEviction(MarketEviction.deserialize(ownership.get(KEY_EVICTION), this));
            }
        } else {
            owner = null;
            purchaseDate = UNSET;
            setEviction(null);
            members.clear();
            mergedName = null;
            memberEditingAllowed = true;
        }

        entrances.addAll(json.getList(KEY_ENTRANCES, ShopEntrance::deserialize));
        connected.addAll(json.getList(KEY_CONNECTED, JsonElement::getAsString));

        if (json.has(KEY_SCANS)) {
            scans.addAll(
                    json.getList(KEY_SCANS, MarketScan::deserialize)
            );
        }
        
        if (json.has(KEY_RESET)) {
            reset = MarketReset.deserialize(json.get(KEY_RESET));
        }
    }

    /* ----------------------------- OBJECT OVERRIDES ------------------------------ */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MarketShop shop = (MarketShop) o;

        return new EqualsBuilder()
                .append(worldGuard, shop.worldGuard)
                .append(getOwner(), shop.getOwner())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(worldGuard)
                .append(getOwner())
                .toHashCode();
    }
}