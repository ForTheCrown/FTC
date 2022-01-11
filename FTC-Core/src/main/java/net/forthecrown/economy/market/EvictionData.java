package net.forthecrown.economy.market;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;

public class EvictionData implements JsonSerializable, Runnable {
    private final MarketShop shop;
    private final Date evictionDate;
    private BukkitTask evictTask;

    public EvictionData(MarketShop shop, Date evictionDate) {
        this.shop = shop;
        this.evictionDate = evictionDate;
    }

    public EvictionData of(JsonElement element, MarketShop shop)  {
        return new EvictionData(shop, JsonUtils.readDate(element));
    }

    public MarketShop getShop() {
        return shop;
    }

    public void start() {
        if(TimeUtil.isPast(evictionDate.getTime())) {
            run();
            return;
        }

        long until = TimeUtil.timeUntil(evictionDate.getTime());
        until = TimeUtil.millisToTicks(until);

        evictTask = Bukkit.getScheduler().runTaskLater(Crown.inst(), this, until);
    }

    public void cancel() {
        if(evictTask == null || evictTask.isCancelled()) return;
        evictTask.cancel();
        evictTask = null;
    }

    @Override
    public JsonElement serialize() {
        return JsonUtils.writeDate(evictionDate);
    }

    @Override
    public void run() {
        Crown.getMarkets().unclaim(getShop(), true);
    }

    public Date getDate() {
        return evictionDate;
    }
}
