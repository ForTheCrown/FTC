package net.forthecrown.core.config;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Worlds;
import org.bukkit.Location;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@ConfigData(filePath = "config.json")
public @UtilityClass class GeneralConfig {
    /* ----------------------------- CONSTANTS ------------------------------ */

    private static transient final Location DEFAULT_SPAWN = new Location(Worlds.overworld(), 267.5, 77.0, 267.5, -180.0F, 0.0f);

    /* ----------------------------- CONFIG FIELDS ------------------------------ */

    public static String
            onFirstJoinKit              = "noobs",
            discordLink                 = "https://discord.gg/wXjHNdp",
            defaultBanReason            = "This server is not for you";

    public static byte
            maxNickLength               = 16,
            maxBossDifficulty           = 7;

    public static short
            nearRadius                  = 200,
            hoppersInOneChunk           = 128,
            broadcastDelay              = 12000;

    public static float
            durabilityWarnThreshold     = 0.1F;

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
            allowMaxPlayerRandomization = true,

            /**
             * Determines whether chunk loader runs in parallel or in series.
             * <p>
             * This means that if this value is true, it'll only load 1 section
             * at a time, instead of running all the world's sections in
             * parallel
             */
            chunkLoaderRunsInSeries     = true,

            debugLoggerEnabled          = FTC.inDebugMode();

    public static int
            effectCost_arrow            = 1000,
            effectCost_death            = 2000,
            effectCost_travel           = 2500,
            swordGoalGainPerKill        = 1,
            tpTickDelay                 = 60,
            tpCooldown                  = 60,
            tpaExpiryTime               = 2400,
            startRhines                 = 100,
            baronPrice                  = 500_000,
            maxUserMapValue             = 50_000_000,
            maxSignShopPrice            = 1_000_000,
            dailySellShopPriceLoss      = 5000;

    public static long
            autoSaveInterval            = TimeUnit.MINUTES.toMillis(30),
            marriageCooldown            = TimeUnit.DAYS.toMillis(3),
            afkKickDelay                = TimeUnit.HOURS.toMillis(3),
            autoAfkDelay                = TimeUnit.HOURS.toMillis(1),
            dataRetentionTime           = TimeUnit.DAYS.toMillis(7 * 2),
            shopUnloadDelay             = TimeUnit.MINUTES.toMillis(5),
            validInviteTime             = TimeUnit.MINUTES.toMillis(10);

    @Setter
    private Location
            serverSpawn                 = DEFAULT_SPAWN.clone();

    @Getter
    public Set<String>
            illegalWorlds               = new ObjectOpenHashSet<>();

    /* ----------------------------- METHODS ------------------------------ */

    public Location getServerSpawn() {
        // If current spawn null -> set spawn to default constant, else return current spawn
        return (serverSpawn == null ? serverSpawn = DEFAULT_SPAWN.clone() : serverSpawn).clone();
    }
}