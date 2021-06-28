package net.forthecrown.core;

import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.comvars.types.ComVarType;
import net.forthecrown.comvars.types.KeyComVarType;
import net.forthecrown.comvars.types.WorldComVarType;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;

public final class ComVars {
    static ComVar<Key>              onFirstJoinKit;

    static ComVar<World>            treasureWorld;

    static ComVar<Byte>             maxNickLength;
    static ComVar<Byte>             maxBossDifficulty;

    static ComVar<Short>            nearRadius;
    static ComVar<Short>            hoppersInOneChunk;

    static ComVar<Long>             marriageCooldown;
    static ComVar<Long>             userDataResetInterval;
    static ComVar<Long>             branchSwapCooldown;
    static ComVar<Long>             auctionExpirationTime;
    static ComVar<Long>             auctionPickupTime;

    static ComVar<Boolean>          allowOtherPlayerNicks;
    static ComVar<Boolean>          taxesEnabled;
    static ComVar<Boolean>          logAdminShop;
    static ComVar<Boolean>          logNormalShop;
    static ComVar<Boolean>          crownEventActive;
    static ComVar<Boolean>          crownEventIsTimed;

    static ComVar<Integer>          tpTickDelay;
    static ComVar<Integer>          tpCooldown;
    static ComVar<Integer>          tpaExpiryTime;
    static ComVar<Integer>          startRhines;
    static ComVar<Integer>          baronPrice;
    static ComVar<Integer>          chickenLevitation;
    static ComVar<Integer>          chickenLevitationTime;
    static ComVar<Integer>          ghspecialReward;
    static ComVar<Integer>          ghfinalReward;
    static ComVar<Integer>          maxMoneyAmount;
    static ComVar<Integer>          maxTreasurePrize;
    static ComVar<Integer>          minTreasurePrize;

    private ComVars() {}

    static void reload(Configuration config){
        maxNickLength = register(           "core_maxNickLength",               ComVarType.BYTE,            (byte) config.getInt("MaxNickLength"));
        maxBossDifficulty = register(       "royals_maxBossDif",                ComVarType.BYTE,            (byte) config.getInt("MaxBossDifficulty"));

        nearRadius = register(              "core_nearRadius",                  ComVarType.SHORT,           (short) config.getInt("NearRadius"));
        hoppersInOneChunk = register(       "core_maxHoppersPerChunk",          ComVarType.SHORT,           (short) config.getInt("HoppersInOneChunk"));

        branchSwapCooldown = register(      "core_branchSwapInterval",          ComVarType.LONG,            config.getLong("BranchSwapCooldown"));
        marriageCooldown = register(        "core_marriageCooldown",            ComVarType.LONG,            config.getLong("MarriageStatusCooldown"));
        userDataResetInterval = register(   "core_userEarningsResetInterval",   ComVarType.LONG,            config.getLong("UserDataResetInterval"));
        auctionExpirationTime = register(   "pr_auctionExpirationTime",         ComVarType.LONG,            config.getLong("Auctions.ExpirationTime"));
        auctionPickupTime = register(       "pr_auctionPickupTime",             ComVarType.LONG,            config.getLong("Auctions.PickUpTime"));

        taxesEnabled = register(            "core_taxesEnabled",                ComVarType.BOOLEAN,         config.getBoolean("Taxes"));
        logAdminShop = register(            "core_log_admin",                   ComVarType.BOOLEAN,         config.getBoolean("Shops.log-admin-purchases"));
        logNormalShop = register(           "core_log_normal",                  ComVarType.BOOLEAN,         config.getBoolean("Shops.log-normal-purchases"));
        allowOtherPlayerNicks = register(   "core_allowOtherPlayerNicks",       ComVarType.BOOLEAN,         config.getBoolean("AllowOtherPlayerNicks"));
        crownEventActive = register(        "core_crownEventActive",            ComVarType.BOOLEAN,         config.getBoolean("CrownEventActive"));
        crownEventIsTimed = register(       "core_eventScoreIsTimer",           ComVarType.BOOLEAN,         config.getBoolean("EventScoreIsTimer"));

        maxMoneyAmount = register(          "core_maxMoneyAmount",              ComVarType.INTEGER,         config.getInt("MaxMoneyAmount"));
        tpTickDelay = register(             "core_tpTickDelay",                 ComVarType.INTEGER,         config.getInt("TeleportTickDelay"));
        tpCooldown = register(              "core_tpCooldown",                  ComVarType.INTEGER,         config.getInt("TeleportCooldown"));
        tpaExpiryTime = register(           "core_tpaExpiryTime",               ComVarType.INTEGER,         config.getInt("TpaExpiryTime"));
        startRhines = register(             "core_startRhines",                 ComVarType.INTEGER,         config.getInt("StartRhines"));
        baronPrice = register(              "core_baronPrice",                  ComVarType.INTEGER,         config.getInt("BaronPrice"));
        chickenLevitation = register(       "core_chickenLevitation",           ComVarType.INTEGER,         config.getInt("MiniGameRegion.ChickenLevitation"));
        chickenLevitationTime = register(   "core_chickenLevitation_time",      ComVarType.INTEGER,         config.getInt("MiniGameRegion.ChickenLevitationTime"));
        ghspecialReward = register(         "pr_gh_specialReward",              ComVarType.INTEGER,         config.getInt("Pirates.SpecialReward"));
        ghfinalReward = register(           "pr_gh_finalReward",                ComVarType.INTEGER,         config.getInt("Pirates.FinalReward"));
        maxTreasurePrize = register(        "pr_maxTreasurePrize",              ComVarType.INTEGER,         config.getInt("Pirates.MaxTreasurePrize"));
        minTreasurePrize = register(        "pr_minTreasurePrize",              ComVarType.INTEGER,         config.getInt("Pirates.MinTreasurePrize"));

        treasureWorld = register(           "pr_treasureWorld",                 WorldComVarType.WORLD,      Bukkit.getWorld(config.getString("Pirates.TreasureWorld")));
        onFirstJoinKit = register(          "core_onJoinKit",                   KeyComVarType.KEY,          Key.key(CrownCore.inst(), config.getString("OnJoinKit")));
    }
    
    private static <T> ComVar<T> register(String name, ComVarType<T> type, T value){
        return ComVarRegistry.set(name, type, value);
    }

    static void save(Configuration config){
        config.set("Taxes",                                 taxesEnabled.getValue());
        config.set("BranchSwapCooldown",                    branchSwapCooldown.getValue());
        config.set("UserDataResetInterval",                 userDataResetInterval.getValue());
        config.set("Shops.log-normal-purchases",            logNormalShop.getValue());
        config.set("Shops.log-admin-purchases",             logAdminShop.getValue());
        config.set("HoppersInOneChunk",                     hoppersInOneChunk.getValue());
        config.set("MaxMoneyAmount",                        maxMoneyAmount.getValue());
        config.set("TeleportTickDelay",                     tpTickDelay.getValue());
        config.set("TeleportCooldown",                      tpCooldown.getValue());
        config.set("TpaExpiryTime",                         tpaExpiryTime.getValue());
        config.set("OnJoinKit",                             onFirstJoinKit.getValue().value());
        config.set("StartRhines",                           startRhines.getValue(100));
        config.set("MaxNickLength",                         maxNickLength.getValue((byte) 16));
        config.set("AllowOtherPlayerNicks",                 allowOtherPlayerNicks.getValue());
        config.set("BaronPrice",                            baronPrice.getValue());
        config.set("NearRadius",                            nearRadius.getValue());
        config.set("MarriageStatusCooldown",                marriageCooldown.getValue());
        config.set("MiniGameRegion.ChickenLevitation",      chickenLevitation.getValue());
        config.set("MiniGameRegion.ChickenLevitationTime",  chickenLevitationTime.getValue());
        config.set("Auctions.ExpirationTime",               auctionExpirationTime.getValue());
        config.set("Auctions.PickUpTime",                   auctionPickupTime.getValue());
        config.set("Pirates.MaxTreasurePrize",              maxTreasurePrize.getValue());
        config.set("Pirates.MinTreasurePrize",              minTreasurePrize.getValue());
        config.set("Pirates.FinalReward",                   ghfinalReward.getValue());
        config.set("Pirates.SpecialReward",                 ghspecialReward.getValue());
        config.set("MaxBossDifficulty",                     maxBossDifficulty.getValue());
    }
}
