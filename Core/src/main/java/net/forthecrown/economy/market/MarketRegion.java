package net.forthecrown.economy.market;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface MarketRegion {
    FtcMarketShop get(UUID owner);
    FtcMarketShop get(String claimName);

    void add(FtcMarketShop claim);

    void attemptPurchase(FtcMarketShop claim, Player player);

    void eject(UUID owner);

    default void remove(FtcMarketShop s) {
        remove(s.getWorldGuardRegion().getId());
    }

    void remove(String name);

    void clear();

    int size();
}
