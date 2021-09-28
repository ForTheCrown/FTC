package net.forthecrown.economy.market;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.MarketOwnership;
import org.bukkit.World;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface Markets {
    MarketShop get(UUID owner);
    MarketShop get(String claimName);

    World getWorld();

    void add(MarketShop claim);

    void attemptPurchase(MarketShop claim, CrownUser user) throws CommandSyntaxException;

    void claim(MarketShop shop, CrownUser user);
    void unclaim(MarketShop shop, boolean complete);

    void merge(MarketShop shop, MarketShop merged);
    void unmerge(MarketShop shop);

    void trust(MarketShop shop, UUID uuid);
    void untrust(MarketShop shop, UUID uuid);

    void removeEntrance(MarketShop shop, int index);
    void addEntrance(MarketShop shop, ShopEntrance entrance);

    boolean areConnected(MarketShop shop, MarketShop other);
    void connect(MarketShop shop, MarketShop other);
    void disconnect(MarketShop shop, MarketShop other);

    void transfer(MarketShop shop, UUID target);

    default void unclaim(UUID owner, boolean eviction) {
        unclaim(get(owner), eviction);
    }

    default void remove(String name) {
        remove(get(name));
    }

    default boolean isEmpty() {
        return getAllShops().isEmpty();
    }

    void remove(MarketShop shop);

    void clear();

    int size();

    Set<UUID> getOwners();
    Set<String> getNames();
    Collection<MarketShop> getAllShops();
    Collection<MarketShop> getOwnedShops();

    static void checkCanChangeStatus(MarketOwnership ownership) throws CommandSyntaxException {
        if(!ownership.canChangeStatus()) {
            long remaining = System.currentTimeMillis() - ownership.getLastStatusChange();

            throw FtcExceptionProvider.translatable("market.cannotChangeStatus", FtcFormatter.millisIntoTime(remaining));
        }
    }
}
