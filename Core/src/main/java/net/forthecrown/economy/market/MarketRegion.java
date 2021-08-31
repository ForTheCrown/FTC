package net.forthecrown.economy.market;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface MarketRegion {
    MarketShop get(UUID owner);
    MarketShop get(String claimName);

    void add(MarketShop claim);

    void attemptPurchase(MarketShop claim, Player player);
    void evict(MarketShop shop);

    void evict(UUID owner);

    default void remove(MarketShop s) {
        remove(s.getWorldGuard().getId());
    }

    void remove(String name);

    void clear();

    int size();

    Set<UUID> getOwners();
}
