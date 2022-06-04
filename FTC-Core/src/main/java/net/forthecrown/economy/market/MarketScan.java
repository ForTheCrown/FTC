package net.forthecrown.economy.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.shops.ShopInventory;
import net.forthecrown.economy.shops.ShopManager;
import net.forthecrown.economy.shops.SignShop;
import net.forthecrown.inventory.ItemStacks;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;

import java.util.List;

@RequiredArgsConstructor
public class MarketScan implements JsonSerializable {
    public static final Var<Byte> REQUIRED_RATIO = Var.def("markets_requiredStockPercent", VarTypes.BYTE, (byte) 20);
    public static final Var<Long> SCAN_INTERVAL = Var.def("markets_scanInterval", VarTypes.TIME, TimeUtil.WEEK_IN_MILLIS * 2);
    public static final Var<Short> MIN_SHOP_AMOUNT = Var.def("markets_minShopAmount", VarTypes.SHORT, (short) 5);
    public static final Var<Long> INACTIVITY_TIME = Var.def("markets_maxInactivity", VarTypes.TIME, TimeUtil.WEEK_IN_MILLIS * 3);

    public static final int
        RES_NOT_STOCKED      = 0,
        RES_PASSED           = 1,
        RES_NOT_ENOUGH_SHOPS = 2,
        RES_INACTIVE         = 3;

    public static final Component[] REASONS = {
            Component.text("Shops are not stocked"),
            null,
            Component.text("Too little sign shops"),
            Component.text("Shop owner(s) are inactive")
    };

    private final List<SingleShopScan> shopScans;
    @Getter private final long scanTime;

    public int size() {
        return shopScans.size();
    }

    public int getResult() {
        if (shopScans.size() < MIN_SHOP_AMOUNT.get()) {
            return RES_NOT_ENOUGH_SHOPS;
        }

        SingleShopScan combined = combineAllScans();
        int size = size();

        SingleShopScan avg = combined.average(size);
        float fillPercent = (float) avg.occupied / avg.totalSpace;
        float requiredPercent = (float) avg.required / avg.totalSpace;

        if (requiredPercent > fillPercent) {
            return RES_NOT_STOCKED;
        }

        if (combined.lastEdit != -1 && TimeUtil.timeSince(combined.lastEdit) > INACTIVITY_TIME.get()) {
            return RES_INACTIVE;
        }

        return RES_PASSED;
    }

    public boolean scanPasses() {
        return getResult() == RES_PASSED;
    }

    private SingleShopScan combineAllScans() {
        SingleShopScan result = SingleShopScan.EMPTY;

        for (var s: shopScans) {
            result = result.combine(s);
        }

        return result;
    }

    public static MarketScan scanArea(WorldBounds3i area) {
        ObjectList<SingleShopScan> scans = new ObjectArrayList<>();
        long time = System.currentTimeMillis();

        for (Block b: area) {
            if (!ShopManager.isShop(b)) continue;

            SignShop shop = Crown.getShopManager().getShop(b.getLocation());
            ShopInventory inv = shop.getInventory();

            int totalSpace = inv.getSize() * inv.getExampleItem().getType().getMaxStackSize();
            int required = inv.getExampleItem().getAmount();
            int occupiedSpace = 0;

            for (var i: inv) {
                if (ItemStacks.isEmpty(i)) continue;

                occupiedSpace += i.getAmount();
            }

            scans.add(new SingleShopScan(totalSpace, required, occupiedSpace, shop.getLastStockEdit()));
        }

        return new MarketScan(scans, time);
    }

    @Override
    public JsonElement serialize() {
        JsonArray array = new JsonArray();

        array.add(getScanTime());

        for (var s: shopScans) {
            JsonArray scanArr = new JsonArray();
            scanArr.add(s.totalSpace);
            scanArr.add(s.required);
            scanArr.add(s.occupied);

            if (s.lastEdit != -1) {
                scanArr.add(s.lastEdit);
            }

            array.add(scanArr);
        }

        return array;
    }

    public static MarketScan deserialize(JsonElement element) {
        JsonArray array = element.getAsJsonArray();

        long time = array.get(0).getAsLong();
        List<SingleShopScan> scans = new ObjectArrayList<>();

        if (array.size() > 1) {
            for (int i = 1; i < array.size(); i++) {
                JsonArray scanArr = array.get(i).getAsJsonArray();

                var s = new SingleShopScan(
                        scanArr.get(0).getAsInt(),
                        scanArr.get(1).getAsInt(),
                        scanArr.get(2).getAsInt(),
                        scanArr.size() == 3 ? -1 : scanArr.get(3).getAsLong()
                );

                scans.add(s);
            }
        }

        return new MarketScan(scans, time);
    }

    public record SingleShopScan(int totalSpace, int required, int occupied, long lastEdit) {
        public static final SingleShopScan EMPTY = new SingleShopScan(0, 0, 0, -1L);

        SingleShopScan combine(SingleShopScan o) {
            return new SingleShopScan(
                    totalSpace + o.totalSpace,
                    required + o.required,
                    occupied + o.occupied,
                    Math.max(o.lastEdit, lastEdit)
            );
        }

        SingleShopScan average(int size) {
            return new SingleShopScan(
                    totalSpace / size,
                    required / size,
                    occupied / size,
                    lastEdit
            );
        }
    }
}