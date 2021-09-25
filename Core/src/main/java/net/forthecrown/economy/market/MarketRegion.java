package net.forthecrown.economy.market;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.CrownUser;
import org.bukkit.World;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface MarketRegion {
    MarketShop get(UUID owner);
    MarketShop get(String claimName);

    World getWorld();

    void add(MarketShop claim);

    void attemptPurchase(MarketShop claim, CrownUser user) throws CommandSyntaxException;
    void unclaim(MarketShop shop, boolean eviction);
    void merge(MarketShop shop, MarketShop merged);

    default void unclaim(UUID owner, boolean eviction) {
        unclaim(get(owner), eviction);
    }

    default void remove(MarketShop s) {
        remove(s.getWorldGuard().getId());
    }

    void remove(String name);

    void clear();

    int size();

    Set<UUID> getOwners();
    Set<String> getNames();
    Collection<MarketShop> getAllShops();
    Collection<MarketShop> getOwnedShops();
}
