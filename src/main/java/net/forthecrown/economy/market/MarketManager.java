package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Crown;
import net.forthecrown.core.DayChangeListener;
import net.forthecrown.core.Vars;
import net.forthecrown.core.Worlds;
import net.forthecrown.user.User;
import net.forthecrown.user.data.TimeField;
import net.forthecrown.user.data.UserMarketData;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.math.WorldVec3i;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MarketManager implements DayChangeListener {
    private static final Logger LOGGER = Crown.logger();

    //2 maps for tracking shops, byName stores all saved shops
    private final Map<UUID, MarketShop> byOwner = new Object2ObjectOpenHashMap<>();
    private final Map<String, MarketShop> byName = new Object2ObjectOpenHashMap<>();

    @Getter
    private final Path directory;

    public MarketManager(Path economyDirectory) {
        this.directory = PathUtil.ensureDirectoryExists(economyDirectory.resolve("markets"))
                .orThrow();
    }

    /* ----------------------------- STATIC METHODS ------------------------------ */

    /**
     * Checks if the given ownership can change status and throws an exception
     * if they cant
     * <p>
     * Ye idk lol, didn't know where else to put stuff this
     * @param ownership The market ownership to check
     * @throws CommandSyntaxException If the given owner can't change status
     */
    public static void checkStatusChange(UserMarketData ownership) throws CommandSyntaxException {
        checkStatusChange(
                ownership,
                "You cannot currently do this, next allowed: {0, time, -timestamp}."
        );
    }

    public static void checkCanPurchase(UserMarketData ownership) throws CommandSyntaxException {
        checkStatusChange(
                ownership,
                "Cannot purchase shop right now, allowed in: {0, time, -timestamp}."
        );
    }

    public static void checkStatusChange(UserMarketData ownership, String transKey) throws CommandSyntaxException {
        if (canChangeStatus(ownership.getUser())) {
            return;
        }

        long nextAllowed = ownership.getUser()
                .getTime(TimeField.MARKET_LAST_ACTION) + Vars.marketStatusCooldown;

        throw Exceptions.format(transKey, nextAllowed);
    }

    /**
     * Tests if this user currently owns a shop
     * @param user The user to test
     * @return True if the user directly owns a market shop
     */
    public static boolean ownsShop(User user) {
        return Crown.getEconomy().getMarkets().get(user.getUniqueId()) != null;
    }

    /**
     * Tests if this user's {@link Vars#marketStatusCooldown} has
     * ended or not
     * @param user The user to test
     * @return True, if the market cooldown has ended for this user
     */
    public static boolean canChangeStatus(User user) {
        return Time.isPast(
                Vars.marketStatusCooldown + user.getTime(TimeField.MARKET_LAST_ACTION)
        );
    }

    /* ----------------------------- METHODS ------------------------------ */

    @Override
    public void onDayChange(ZonedDateTime time) {
        if (time.getDayOfWeek() != DayOfWeek.MONDAY) {
            return;
        }

        for (var market: byOwner.values()) {
            market.validateOwnership(this);
        }
    }

    /**
     * Gets a shop by the shop owner's UUID
     * @param owner The owner
     * @return The shop owned by that UUID, null if the UUID has no saved shop
     */
    public MarketShop get(UUID owner) {
        return byOwner.get(owner);
    }

    /**
     * Gets a shop by the name of it's WorldGuard region
     * @param claimName The shop's name
     * @return The shop by that name
     */
    public MarketShop get(String claimName) {
        return byName.get(claimName);
    }

    public MarketShop get(WorldVec3i pos) {
        if (!pos.getWorld().equals(getWorld())) {
            return null;
        }

        for (var s: getAllShops()) {
            if (s.getWorldGuard().contains(pos.x(), pos.y(), pos.z())) {
                return s;
            }
        }

        return null;
    }

    /**
     * Gets the world the markets are in
     * @return The market's world
     */
    public World getWorld() {
        return Worlds.overworld();
    }

    /**
     * Add the given shop to this manager's control
     * @param claim The shop to add
     */
    public void add(MarketShop claim) {
        if(claim.hasOwner()) {
            byOwner.put(claim.getOwner(), claim);
        }

        byName.put(claim.getName(), claim);

        // Sync world guard so its members
        // list is the same as the shop's
        claim.syncWorldGuard();
    }

    void onShopClaim(MarketShop shop) {
        byOwner.put(shop.getOwner(), shop);
    }

    void onShopUnclaim(MarketShop shop) {
        byOwner.remove(shop.getOwner());
    }

    /**
     * Removes the given shop from from this market manager
     * @param shop The shop to remove
     */
    public void remove(MarketShop shop) {
        _remove(shop);

        for (ShopEntrance e: shop.getEntrances()) {
            e.removeNotice(getWorld());
            e.removeSign(getWorld());
        }

        ProtectedRegion region = shop.getWorldGuard();

        if (shop.isMerged()) {
            shop.unmerge();
        }

        for (var m: shop.getConnected()) {
            var connected = get(m);

            if (connected == null) {
                continue;
            }

            shop.disconnect(connected);
        }

        if (shop.hasOwner()) {
            User user = shop.ownerUser();
            shop.setOwner(null);

            user.getTimeTracker()
                    .remove(TimeField.MARKET_OWNERSHIP_STARTED);

            region.getMembers().clear();
        }
    }

    private void _remove(MarketShop shop) {
        if (shop.hasOwner()) {
            byOwner.remove(shop.getOwner());
        }
        byName.remove(shop.getName());
    }

    /**
     * Clears all the shops from this manager
     */
    public void clear() {
        for (MarketShop m: getAllShops()) {
            m.setEviction(null);
        }

        byName.clear();
        byOwner.clear();
    }

    /**
     * Gets the amount of shops this manager has
     * @return The shops held by this manager
     */
    public int size() {
        return byName.size();
    }

    /**
     * Gets all the UUIDs that currently own shops
     * @return All shop owners' UUIDs
     */
    public Set<UUID> getOwners() {
        return byOwner.keySet();
    }

    /**
     * Gets the names of all the shops
     * @return The shops' names
     */
    public Set<String> getNames() {
        return byName.keySet();
    }

    /**
     * Gets all the shops held by this manager
     * @return All this manager's shops
     */
    public Collection<MarketShop> getAllShops() {
        return byName.values();
    }

    public boolean isEmpty() {
        return getAllShops().isEmpty();
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    public void load() {
        clear();

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(getWorld()));

        Set<MarketShop> notLoaded = new ObjectOpenHashSet<>(byName.values());

        try (var stream = Files.newDirectoryStream(directory)) {
            for (var p: stream) {
                String name = p.getFileName()
                        .toString()
                        .replaceAll(".json", "");

                if (!manager.hasRegion(name)) {
                    LOGGER.warn("Found shop with no matching worldguard region: file='{}', name='{}'", p, name);
                    continue;
                }

                try {
                    JsonElement element = JsonUtils.readFile(p);
                    MarketShop shop = get(name);

                    if (shop == null) {
                        // If the shop doesn't exist, create a shop
                        // instance for it
                        ProtectedRegion region = manager.getRegion(name);
                        shop = new MarketShop(region);
                    } else {
                        // Temporarily remove shop
                        // because owner or members might
                        // change and the add(shop); call
                        // will sync all that data again
                        _remove(shop);
                    }

                    shop.deserialize(element);
                    notLoaded.remove(shop);
                    add(shop);
                } catch (IOException e) {
                    LOGGER.error("Error reading market file: '{}'", p, e);
                }
            }

            if (!notLoaded.isEmpty()) {
                for (var shop: notLoaded) {
                    remove(shop);
                }
            }
        } catch (IOException e) {
            LOGGER.error("Error loading market shops:", e);
        }
    }

    public void save() {
        for (var market: byName.values()) {
            Path marketFile = directory.resolve(market.getName() + ".json");
            SerializationHelper.writeJsonFile(marketFile, market::serialize);
        }
    }
}