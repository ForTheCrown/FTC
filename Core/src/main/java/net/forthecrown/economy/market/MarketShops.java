package net.forthecrown.economy.market;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface MarketShops {
    MarketShop get(UUID owner);
    MarketShop get(String claimName);

    void add(MarketShop claim);

    void attemptPurchase(MarketShop claim, Player player);

    void eject(UUID owner);

    default void remove(MarketShop s) {
        remove(s.getWorldGuardRegion().getId());
    }

    void remove(String name);

    void clear();

    int size();
}
