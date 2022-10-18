package net.forthecrown.datafix;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.market.MarketReset;
import net.forthecrown.economy.market.MarketScan;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.economy.market.ShopEntrance;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.PathUtil;
import org.spongepowered.math.vector.Vector3i;

import java.nio.file.Path;

public class MarketsUpdate extends DataUpdater {
    @Override
    protected void createRenames(ImmutableMap.Builder<String, String> builder) {
        builder
                .put("ownershipData", MarketShop.KEY_CURRENT_OWNER)
                .put("coOwners", MarketShop.KEY_MEMBERS)
                .put("dateOfPurchase", MarketShop.KEY_PURCHASE_DATE)
                .put("notice", ShopEntrance.KEY_NOTICE_POS)
                .put("membersCanEditShops", MarketShop.KEY_EDITING);
    }

    @Override
    protected boolean update() throws Throwable {
        Path oldPath = PathUtil.pluginPath("market_region.json");
        JsonObject obj = JsonUtils.readFileObject(oldPath);
        obj = rename(JsonOps.INSTANCE, obj, true)
                .orThrow()
                .getAsJsonObject();

        JsonArray arr = obj.getAsJsonArray("entries");

        RegionManager manager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(Crown.getEconomy().getMarkets().getWorld()));

        for (var e: arr) {
            JsonObject marketJson = e.getAsJsonObject();
            String name = marketJson.get("name").getAsString();

            ProtectedRegion region = manager.getRegion(marketJson.get(MarketShop.KEY_NAME).getAsString());

            if (region == null) {
                LOGGER.warn("Found market '{}' with no matching world guard region!", name);
                continue;
            }

            if (marketJson.has(MarketShop.KEY_CURRENT_OWNER)) {
                var currentOwner = marketJson.getAsJsonObject(MarketShop.KEY_CURRENT_OWNER);

                if (currentOwner.has("scans")) {
                    marketJson.add(MarketShop.KEY_SCANS, fixScans(currentOwner.getAsJsonArray("scans")));
                    currentOwner.remove("scans");
                }

                marketJson.add(MarketShop.KEY_CURRENT_OWNER, currentOwner);
            }

            Vector3i wgMin = Vectors.from(region.getMinimumPoint());
            Vector3i wgMax = Vectors.from(region.getMaximumPoint());
            Vector3i copyPos = wgMin.sub(0, 30, 0);
            Vector3i copySize = wgMax.sub(wgMin);

            MarketReset reset = new MarketReset(wgMin, copyPos, copySize);
            marketJson.add(MarketShop.KEY_RESET, reset.serialize());

            Path newPath = Crown.getEconomy()
                    .getMarkets()
                    .getDirectory()
                    .resolve(name + ".json");

            JsonUtils.writeFile(marketJson, newPath);

            LOGGER.info("Moved shop '{}' to its own file", name);
        }

        return PathUtil.safeDelete(oldPath, true, true)
                .result()
                .isPresent();
    }

    // Note:
    //
    // Pre-update Scans use a very obtuse serialization system
    // of an array of array arrays... Yes. Essentially, all scans
    // are in a single array, each scan is serialized as an array,
    // the first entry in that array is the timestamp of when the
    // scan was conducted, the rest of the entries are arrays of
    // a single shop's `scan`. This array holds 4 values, all numbers,
    // The array entries are as follows:
    //
    // 0) The total amount of itemstack space in the inventory
    // 1) The minimum amount of items required for the shop to count as stocked
    // 2) The inventory item count
    // 3) The timestamp of the last time the shop was edited
    //
    // Values 0 and 3 are worthless and thus we need to update scans
    // to the new format using values 1 and 2, this is just done
    // by comparing them against eachother, if required count is
    // higher than item count, unstocked, else stocked.
    //   -- Jules <3

    private JsonArray fixScans(JsonArray source) {
        JsonArray result = new JsonArray();

        for (var e: source) {
            JsonArray arr = e.getAsJsonArray();
            MarketScan single = fixSingleScan(arr);

            result.add(single.serialize());
        }

        return result;
    }

    private MarketScan fixSingleScan(JsonArray array) {
        long date = array.get(0).getAsLong();
        array.remove(0);

        int stocked = 0;
        int unstocked = 0;

        for (var e: array) {
            JsonArray singleShop = e.getAsJsonArray();

            int requiredItemCount = singleShop.get(1).getAsInt();
            int itemCount = singleShop.get(2).getAsInt();

            if (itemCount >= requiredItemCount) {
                stocked++;
            } else {
                unstocked++;
            }
        }

        return new MarketScan(date, stocked, unstocked);
    }
}