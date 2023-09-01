package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.Comparator;
import net.forthecrown.economy.signshops.ShopManager;
import net.forthecrown.economy.signshops.SignShop;
import net.forthecrown.economy.signshops.SignShops;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.World;

public record MarketScan(long date, int stockedCount, int unstockedCount, long averageUseDate) {
  /* ----------------------------- CONSTANTS ------------------------------ */

  /**
   * Comparator for sorting market scans by their date, latest to oldest
   */
  static final Comparator<MarketScan> COMPARATOR = Comparator.comparingLong(MarketScan::date)
      .reversed();

  private static final String KEY_DATE = "date";
  private static final String KEY_STOCKED = "stockedCount";
  private static final String KEY_UNSTOCKED = "unstockedCount";
  private static final String KEY_AVERAGE_USE_DATE = "averageUseDate";

  /* ----------------------------- STATIC CONSTRUCTOR ------------------------------ */

  public static MarketScan create(World world, MarketShop shop, ShopManager shops) {
    int stocked = 0;
    int unstocked = 0;
    LongList useDates = new LongArrayList();

    var rg = shop.getWorldGuard();
    WorldBounds3i bounds = new WorldBounds3i(
        world,
        rg.getMinimumPoint().getX(),
        rg.getMinimumPoint().getY(),
        rg.getMinimumPoint().getZ(),
        rg.getMaximumPoint().getX(),
        rg.getMaximumPoint().getY(),
        rg.getMaximumPoint().getZ()
    );

    for (var b : bounds) {
      if (!SignShops.isShop(b)) {
        continue;
      }

      SignShop signShop = shops.getShop(b);

      if (signShop.getType().isAdmin()) {
        continue;
      }

      if (signShop.inStock()) {
        stocked++;
      } else {
        unstocked++;
      }

      long lastUse = signShop.getLastInteraction();

      if (lastUse != -1) {
        useDates.add(lastUse);
      }
    }

    long averageUseDate = (long) useDates.longStream().average().orElse(-1D);

    return new MarketScan(System.currentTimeMillis(), stocked, unstocked, averageUseDate);
  }

  /* ----------------------------- SERIALIZATION ------------------------------ */

  public JsonElement serialize() {
    JsonWrapper json = JsonWrapper.create();

    json.addTimeStamp(KEY_DATE, date);
    json.add(KEY_STOCKED, stockedCount);
    json.add(KEY_UNSTOCKED, unstockedCount);
    json.add(KEY_AVERAGE_USE_DATE, averageUseDate);

    return json.getSource();
  }

  public static MarketScan deserialize(JsonElement element) {
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    return new MarketScan(
        json.getTimeStamp(KEY_DATE),
        json.getInt(KEY_STOCKED),
        json.getInt(KEY_UNSTOCKED),
        json.getLong(KEY_AVERAGE_USE_DATE, -1)
    );
  }
}