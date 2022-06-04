package net.forthecrown.core;

import net.forthecrown.vars.Var;
import net.forthecrown.vars.types.VarTypes;
import net.forthecrown.regions.RegionConstants;
import net.kyori.adventure.key.Key;
import org.bukkit.World;

import static net.forthecrown.vars.VarRegistry.def;
import static net.forthecrown.utils.TimeUtil.*;

public final class FtcVars {
    static Var<Boolean>
            inDebugMode;

    public static final Var<Key>
            onFirstJoinKit              = def("onFirstJoinKit",         VarTypes.KEY,        Keys.forthecrown("noobs"));

    static Var<World>
            regionWorld;

    public static final Var<String>
            spawnRegion                 = def("spawnRegion",            VarTypes.STRING,     RegionConstants.DEFAULT_SPAWN_NAME),
            discordLink                 = def("discordLink",            VarTypes.STRING,     "https://discord.gg/wXjHNdp"),
            defaultBanReason            = def("defaultBanReason",       VarTypes.STRING,      "This server is not for you");

    public static final Var<Byte>
            maxNickLength               = def("maxNickLength",          VarTypes.BYTE,       (byte) 16),
            maxBossDifficulty           = def("maxBossDifficulty",      VarTypes.BYTE,       (byte) 7);

    public static final Var<Short>
            nearRadius                  = def("nearRadius",             VarTypes.SHORT,      (short) 200),
            hoppersInOneChunk           = def("hoppersInOneChunk",      VarTypes.SHORT,      (short) 128),
            maxGuildMembers             = def("maxGuildMembers",        VarTypes.SHORT,      (short) 7);

    public static final Var<Float>
            houses_startingDemand       = def("houses_startingDemand",  VarTypes.FLOAT,      0.0F),
            guildWageModifier           = def("guildWageModifier",      VarTypes.FLOAT,      0.05F),
            rwDoubleDropRate            = def("rwDoubleDropRate",       VarTypes.FLOAT,      0.5F);

    public static final Var<Long>
            marriageCooldown            = def("marriageCooldown",       VarTypes.TIME,       DAY_IN_MILLIS * 3),
            userDataResetInterval       = def("userDataResetInterval",  VarTypes.TIME,       MONTH_IN_MILLIS * 2),
            autoSaveIntervalMins        = def("autoSaveIntervalMins",   VarTypes.LONG,       60L),
            marketOwnershipSafeTime     = def("marketOwnershipSafeTime",VarTypes.TIME,       WEEK_IN_MILLIS * 2),
            evictionCleanupTime         = def("evictionCleanupTime",    VarTypes.TIME,       DAY_IN_MILLIS * 3),
            voteTime                    = def("voteTime",               VarTypes.TIME,       WEEK_IN_MILLIS),
            voteInterval                = def("voteInterval",           VarTypes.TIME,       WEEK_IN_MILLIS),
            guildJoinRequirement        = def("guildJoinRequirement",   VarTypes.TIME,       MONTH_IN_MILLIS * 2),
            marketStatusCooldown        = def("marketStatusCooldown",   VarTypes.TIME,       DAY_IN_MILLIS * 2),
            resourceWorldResetInterval  = def("rwResetInterval",        VarTypes.TIME,       MONTH_IN_MILLIS * 2),
            afkKickDelay                = def("afkKickDelay",           VarTypes.TIME,       HOUR_IN_MILLIS * 3),
            dataRetentionTime           = def("dataRetentionTime",      VarTypes.TIME,       WEEK_IN_MILLIS * 2),
            guildJoinTime               = def("guildJoinTime",          VarTypes.TIME,       WEEK_IN_MILLIS * 2),
            gb_donorTimeLength          = def("gb_donorTimeLength",     VarTypes.TIME,       MONTH_IN_MILLIS),
            marketAutoKickCooldown      = def("marketAutoKickCooldown", VarTypes.TIME,       WEEK_IN_MILLIS);

    public static final Var<Boolean>
            taxesEnabled                = def("taxesEnabled",           VarTypes.BOOL,       false),
            logAdminShop                = def("logAdminShop",           VarTypes.BOOL,       true),
            logNormalShop               = def("logNormalShop",          VarTypes.BOOL,       false),
            crownEventActive            = def("crownEventActive",       VarTypes.BOOL,       false),
            crownEventIsTimed           = def("crownEventIsTimed",      VarTypes.BOOL,       false),
            hulkSmashPoles              = def("hulkSmashPoles",         VarTypes.BOOL,       true),
            allowNonOwnerSwords         = def("allowNonOwnerSwords",    VarTypes.BOOL,       false),
            bp_extraExpGivesRhines      = def("bp_extraExpGivesRhines", VarTypes.BOOL,       true),
            useAsyncTpForPlayers        = def("useAsyncTpForPlayers",   VarTypes.BOOL,       false),
            staffLogEnabled             = def("staffLogEnabled",        VarTypes.BOOL,       false);

    public static final Var<Integer>
            effectCost_arrow            = def("effectCost_arrow",       VarTypes.INT,        1000),
            effectCost_death            = def("effectCost_death",       VarTypes.INT,        2000),
            effectCost_travel           = def("effectCost_travel",      VarTypes.INT,        2500),
            swordGoalGainPerKill        = def("swordGoalGainPerKill",   VarTypes.INT,        1),
            houses_startingSupply       = def("houses_startingSupply",  VarTypes.INT,        2500),
            tpTickDelay                 = def("tpTickDelay",            VarTypes.INT,        60),
            tpCooldown                  = def("tpCooldown",             VarTypes.INT,        60),
            tpaExpiryTime               = def("tpaExpiryTime",          VarTypes.INT,        2400),
            startRhines                 = def("startRhines",            VarTypes.INT,        100),
            baronPrice                  = def("baronPrice",             VarTypes.INT,        500000),
            maxMoneyAmount              = def("maxMoneyAmount",         VarTypes.INT,        50000000),
            maxShopEarnings             = def("maxShopEarnings",        VarTypes.INT,        150000),
            maxSignShopPrice            = def("maxSignShopPrice",       VarTypes.INT,        1000000),
            defaultMarketPrice          = def("defaultMarketPrice",     VarTypes.INT,        55000),
            guildPayIntervalDays        = def("guildPayIntervalDays",   VarTypes.INT,        14),
            guildBaseWage               = def("guildBaseWage",          VarTypes.INT,        25000);

    private FtcVars() {}

    public static World getRegionWorld() {
        return regionWorld.get();
    }
}