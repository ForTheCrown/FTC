package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.economy.shops.SignShops;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.World;

import java.util.Comparator;

public record MarketScan(long date, int stockedCount, int unstockedCount) {
    /* ----------------------------- CONSTANTS ------------------------------ */

    /** Comparator for sorting market scans by their date, latest to oldest */
    static final Comparator<MarketScan> COMPARATOR = Comparator.comparingLong(MarketScan::date).reversed();

    private static final String
            KEY_DATE = "date",
            KEY_STOCKED = "stockedCount",
            KEY_UNSTOCKED = "unstockedCount";

    /* ----------------------------- STATIC CONSTRUCTOR ------------------------------ */

    public static MarketScan create(World world, MarketShop shop) {
        int stocked = 0;
        int unstocked = 0;

        for (var b: WorldBounds3i.of(world, shop.getWorldGuard())) {
            if (!SignShops.isShop(b)) {
                continue;
            }

            SignShop signShop = Economy.get().getShops().getShop(b);

            if (signShop.getType().isAdmin()) {
                continue;
            }

            if (signShop.inStock()) {
                stocked++;
            } else {
                unstocked++;
            }
        }

        return new MarketScan(System.currentTimeMillis(), stocked, unstocked);
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.create();

        json.addTimeStamp(KEY_DATE, date);
        json.add(KEY_STOCKED, stockedCount);
        json.add(KEY_UNSTOCKED, unstockedCount);

        return json.getSource();
    }

    public static MarketScan deserialize(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return new MarketScan(
                json.getTimeStamp(KEY_DATE),
                json.getInt(KEY_STOCKED),
                json.getInt(KEY_UNSTOCKED)
        );
    }
}