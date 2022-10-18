package net.forthecrown.core;

import net.forthecrown.regions.Regions;
import net.forthecrown.vars.Var;

import java.util.concurrent.TimeUnit;

/**
 * The Vars FTC uses stored as constants for easy access
 */
@Var
public final class Vars {
    private Vars() {}

    public static String
            onFirstJoinKit              = "noobs",
            spawnRegion                 = Regions.DEFAULT_SPAWN_NAME,
            discordLink                 = "https://discord.gg/wXjHNdp",
            defaultBanReason            = "This server is not for you";

    public static byte
            maxNickLength               = 16,
            maxBossDifficulty           = 7;

    public static short
            nearRadius                  = 200,
            hoppersInOneChunk           = 128;

    @Var(callback = "broadcastIntervalCallback")
    public static short
            broadcastDelay              = 12000,
            markets_minShopAmount       = 5;

    public static float
            rwDoubleDropRate            = 0.5F,
            durabilityWarnThreshold     = 0.1F,
            markets_minStockRequired    = 0.33F;

    @Var(type = "time_interval")
    public static long
            autoSaveInterval            = TimeUnit.MINUTES.toMillis(30);

    @Var(type = "time_interval")
    public static long
            marriageCooldown            = TimeUnit.DAYS.toMillis(3),
            marketStatusCooldown        = TimeUnit.DAYS.toMillis(2),
            rwResetInterval             = TimeUnit.DAYS.toMillis(28 * 2),
            afkKickDelay                = TimeUnit.HOURS.toMillis(3),
            autoAfkDelay                = TimeUnit.HOURS.toMillis(1),
            dataRetentionTime           = TimeUnit.DAYS.toMillis(7 * 2),
            shopUnloadDelay             = TimeUnit.MINUTES.toMillis(5),
            markets_evictionDelay       = TimeUnit.DAYS.toMillis(7 * 2),
            markets_maxOfflineTime      = TimeUnit.DAYS.toMillis(28),
            rw_sectionRetentionTime     = TimeUnit.MINUTES.toMillis(5);

    public static boolean
            userCacheSuggestions        = true,
            logAdminShop                = true,
            logNormalShop               = false,
            crownEventActive            = false,
            crownEventIsTimed           = false,
            hulkSmashPoles              = true,
            allowNonOwnerSwords         = false,
            useAsyncTpForPlayers        = false,
            staffLogEnabled             = false,
            announcePunishments         = false,
            syncDiscordBansToServer     = true;

    public static int
            effectCost_arrow            = 1000,
            effectCost_death            = 2000,
            effectCost_travel           = 2500,
            swordGoalGainPerKill        = 1,
            tpTickDelay                 = 60,
            tpCooldown                  = 60,
            tpaExpiryTime               = 2400,
            startRhines                 = 100,
            baronPrice                  = 500000,
            maxMoneyAmount              = 50000000,
            maxSignShopPrice            = 1000000,
            defaultMarketPrice          = 55000,
            dailySellShopPriceLoss      = 5000;

    private static void broadcastIntervalCallback() {
        if (Crown.getAnnouncer() == null) {
            return;
        }

        Crown.getAnnouncer().start();
    }
}