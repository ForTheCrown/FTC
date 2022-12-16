package net.forthecrown.economy.market;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.config.ConfigData;

import java.util.concurrent.TimeUnit;

@ConfigData(filePath = "markets.json")
public @UtilityClass class MarketConfig {
    public long
            evictionDelay           = TimeUnit.DAYS.toMillis(7 * 2),
            maxOfflineTime          = TimeUnit.DAYS.toMillis(7 * 4),
            statusCooldown          = TimeUnit.DAYS.toMillis(2),
            scanInterval            = TimeUnit.DAYS.toMillis(7);

    public int
            minShopAmount           = 5,
            defaultPrice            = 55_000;

    public boolean
            autoEvictionsEnabled    = true;

    public float
            minStockRequired        = 0.33F;
}