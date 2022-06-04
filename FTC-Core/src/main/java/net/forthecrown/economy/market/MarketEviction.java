package net.forthecrown.economy.market;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcDiscord;
import net.forthecrown.core.chat.ChatUtils;
import net.forthecrown.serializer.JsonSerializable;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.TimeUtil;
import net.forthecrown.utils.math.WorldBounds3i;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor
public class MarketEviction implements JsonSerializable {
    public static final byte
        CAUSE_COMMAND = 0,
        CAUSE_AUTOMATED = 1;

    public static final int
        ARR_ID_CAUSE = 0,
        ARR_ID_TIME = 1,
        ARR_ID_REASON = 2;

    @Getter private final String marketName;
    @Getter private final byte cause;
    @Getter private final long evictionTime;
    @Getter private final Component reason;

    private BukkitTask task;

    void start() {
        cancel();

        long execTime = TimeUtil.millisToTicks(TimeUtil.timeUntil(evictionTime));

        if (execTime <= 0) {
            run();
            return;
        }

        Bukkit.getScheduler().runTaskLater(Crown.inst(), this::run, execTime);
    }

    void cancel() {
        if (task == null || task.isCancelled()) return;

        task.cancel();
        task = null;
    }

    private void run() {
        Markets markets = Crown.getMarkets();
        MarketShop market = markets.get(marketName);

        if (market == null) {
            Crown.logger().warn("Cannot evict owner of '{}', market does not exist", marketName);
            return;
        }

        if (cause == CAUSE_AUTOMATED) {
            MarketScan scan = MarketScan.scanArea(WorldBounds3i.of(
                    markets.getWorld(),
                    market.getWorldGuard().getMinimumPoint(),
                    market.getWorldGuard().getMaximumPoint()
            ));

            if (scan.scanPasses()) {
                return;
            }
        }

        CrownUser owner = market.ownerUser();

        owner.sendAndMail(
                Component.translatable("market.evict.evicted",
                        NamedTextColor.GRAY,

                        Component.text()
                                .append(reason)
                                .color(NamedTextColor.WHITE)
                                .build()
                )
        );

        FtcDiscord.staffLog("Markets", "{}, owner '{}', evicted", market.getName(), owner.getNickOrName());

        markets.unclaim(market, true);
    }

    @Override
    public JsonElement serialize() {
        JsonArray array = new JsonArray();

        array.add(cause);
        array.add(evictionTime);
        array.add(ChatUtils.toJson(reason));

        return array;
    }

    public static MarketEviction deserialize(JsonArray array, MarketShop shop) {
        return new MarketEviction(
                shop.getName(),
                array.get(ARR_ID_CAUSE).getAsByte(),
                array.get(ARR_ID_TIME).getAsLong(),
                ChatUtils.fromJson(array.get(ARR_ID_REASON))
        );
    }
}