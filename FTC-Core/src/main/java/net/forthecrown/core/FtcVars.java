package net.forthecrown.core;

import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarTypes;
import net.forthecrown.regions.RegionConstants;
import net.kyori.adventure.key.Key;
import org.bukkit.World;

import static net.forthecrown.vars.VarRegistry.getSafe;
import static net.forthecrown.utils.TimeUtil.*;

public final class FtcVars {
    static Var<Boolean>
            inDebugMode;

    public static final Var<Key>
            onFirstJoinKit              = getSafe("onFirstJoinKit",         VarTypes.KEY,        Keys.forthecrown("noobs"));

    static Var<World>
            regionWorld;

    public static final Var<String>
            spawnRegion                 = getSafe("spawnRegion",            VarTypes.STRING,     RegionConstants.DEFAULT_SPAWN_NAME),
            discordLink                 = getSafe("discordLink",            VarTypes.STRING,     "https://discord.gg/wXjHNdp");

    public static final Var<Byte>
            maxNickLength               = getSafe("maxNickLength",          VarTypes.BYTE,       (byte) 16),
            maxBossDifficulty           = getSafe("maxBossDifficulty",      VarTypes.BYTE,       (byte) 7);

    public static final Var<Short>
            nearRadius                  = getSafe("nearRadius",             VarTypes.SHORT,      (short) 200),
            hoppersInOneChunk           = getSafe("hoppersInOneChunk",      VarTypes.SHORT,      (short) 128),
            maxGuildMembers             = getSafe("maxGuildMembers",        VarTypes.SHORT,      (short) 7);

    public static final Var<Float>
            houses_startingDemand       = getSafe("houses_startingDemand",  VarTypes.FLOAT,      0.0F),
            guildWageModifier           = getSafe("guildWageModifier",      VarTypes.FLOAT,      0.05F),
            rwDoubleDropRate            = getSafe("rwDoubleDropRate",       VarTypes.FLOAT,      0.5F);

    public static final Var<Long>
            marriageCooldown            = getSafe("marriageCooldown",       VarTypes.TIME,       DAY_IN_MILLIS * 3),
            userDataResetInterval       = getSafe("userDataResetInterval",  VarTypes.TIME,       MONTH_IN_MILLIS * 2),
            autoSaveIntervalMins        = getSafe("autoSaveIntervalMins",   VarTypes.LONG,       60L),
            marketOwnershipSafeTime     = getSafe("marketOwnershipSafeTime", VarTypes.TIME,       WEEK_IN_MILLIS * 2),
            evictionCleanupTime         = getSafe("evictionCleanupTime",    VarTypes.TIME,       DAY_IN_MILLIS * 3),
            voteTime                    = getSafe("voteTime",               VarTypes.TIME,       WEEK_IN_MILLIS),
            voteInterval                = getSafe("voteInterval",           VarTypes.TIME,       WEEK_IN_MILLIS),
            guildJoinRequirement        = getSafe("guildJoinRequirement",   VarTypes.TIME,       MONTH_IN_MILLIS * 2),
            marketStatusCooldown        = getSafe("marketStatusCooldown",   VarTypes.TIME,       DAY_IN_MILLIS * 2),
            resourceWorldResetInterval  = getSafe("rwResetInterval",        VarTypes.TIME,       MONTH_IN_MILLIS * 2),
            afkKickDelay                = getSafe("afkKickDelay",           VarTypes.TIME,       HOUR_IN_MILLIS * 3),
            dataRetentionTime           = getSafe("dataRetentionTime",      VarTypes.TIME,       WEEK_IN_MILLIS * 2),
            guildJoinTime               = getSafe("guildJoinTime",          VarTypes.TIME,       WEEK_IN_MILLIS * 2),
            gb_donorTimeLength          = getSafe("gb_donorTimeLength",     VarTypes.TIME,       MONTH_IN_MILLIS);

    public static final Var<Boolean>
            taxesEnabled                = getSafe("taxesEnabled",           VarTypes.BOOL,       false),
            logAdminShop                = getSafe("logAdminShop",           VarTypes.BOOL,       true),
            logNormalShop               = getSafe("logNormalShop",          VarTypes.BOOL,       false),
            crownEventActive            = getSafe("crownEventActive",       VarTypes.BOOL,       false),
            crownEventIsTimed           = getSafe("crownEventIsTimed",      VarTypes.BOOL,       false),
            hulkSmashPoles              = getSafe("hulkSmashPoles",         VarTypes.BOOL,       true),
            allowNonOwnerSwords         = getSafe("allowNonOwnerSwords",    VarTypes.BOOL,       false),
            gb_extraExpGivesRhines      = getSafe("gb_extraExpGivesRhines", VarTypes.BOOL,       true);

    public static final Var<Integer>
            effectCost_arrow            = getSafe("effectCost_arrow",       VarTypes.INT,        1000),
            effectCost_death            = getSafe("effectCost_death",       VarTypes.INT,        2000),
            effectCost_travel           = getSafe("effectCost_travel",      VarTypes.INT,        2500),
            swordGoalGainPerKill        = getSafe("swordGoalGainPerKill",   VarTypes.INT,        1),
            houses_startingSupply       = getSafe("houses_startingSupply",  VarTypes.INT,        2500),
            tpTickDelay                 = getSafe("tpTickDelay",            VarTypes.INT,        60),
            tpCooldown                  = getSafe("tpCooldown",             VarTypes.INT,        60),
            tpaExpiryTime               = getSafe("tpaExpiryTime",          VarTypes.INT,        2400),
            startRhines                 = getSafe("startRhines",            VarTypes.INT,        100),
            baronPrice                  = getSafe("baronPrice",             VarTypes.INT,        500000),
            maxMoneyAmount              = getSafe("maxMoneyAmount",         VarTypes.INT,        50000000),
            maxShopEarnings             = getSafe("maxShopEarnings",        VarTypes.INT,        150000),
            maxSignShopPrice            = getSafe("maxSignShopPrice",       VarTypes.INT,        1000000),
            defaultMarketPrice          = getSafe("defaultMarketPrice",     VarTypes.INT,        55000),
            guildPayIntervalDays        = getSafe("guildPayIntervalDays",   VarTypes.INT,        14),
            guildBaseWage               = getSafe("guildBaseWage",          VarTypes.INT,        25000);

    private FtcVars() {}

    public static World getRegionWorld() {
        return regionWorld.get();
    }
}
