package net.forthecrown.core;

import com.google.gson.JsonObject;
import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.comvars.types.ComVarType;
import net.forthecrown.comvars.types.ComVarTypes;
import net.forthecrown.regions.RegionConstants;
import net.forthecrown.serializer.JsonWrapper;
import net.forthecrown.utils.FileDefaults;
import net.forthecrown.utils.JsonUtils;
import net.forthecrown.utils.TimeUtil;
import net.kyori.adventure.key.Key;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

public final class ComVars {
    static ComVar<Boolean>          inDebugMode;

    static ComVar<Key>              onFirstJoinKit;

    static ComVar<World>            regionWorld;

    static ComVar<String>           spawnRegion;
    static ComVar<String>           discordLink;

    static ComVar<Byte>             maxNickLength;
    static ComVar<Byte>             maxBossDifficulty;
    static ComVar<Byte>             maxTreasureItems;

    static ComVar<Short>            nearRadius;
    static ComVar<Short>            hoppersInOneChunk;
    static ComVar<Short>            maxGuildMembers;

    static ComVar<Float>            houses_startingDemand;
    static ComVar<Float>            guildWageModifier;
    static ComVar<Float>            rwDoubleDropRate;

    static ComVar<Long>             marriageCooldown;
    static ComVar<Long>             userDataResetInterval;
    static ComVar<Long>             autoSaveIntervalMins;
    static ComVar<Long>             marketOwnershipSafeTime;
    static ComVar<Long>             evictionCleanupTime;
    static ComVar<Long>             voteTime;
    static ComVar<Long>             voteInterval;
    static ComVar<Long>             guildJoinRequirement;
    static ComVar<Long>             marketStatusCooldown;
    static ComVar<Long>             resourceWorldResetInterval;
    static ComVar<Long>             afkKickDelay;
    static ComVar<Long>             dataRetentionTime;
    static ComVar<Long>             guildJoinTime;
    static ComVar<Long>             gb_donorTimeLength;     // How long do bought gb passes last

    static ComVar<Boolean>          allowOtherPlayerNicks;
    static ComVar<Boolean>          taxesEnabled;
    static ComVar<Boolean>          logAdminShop;
    static ComVar<Boolean>          logNormalShop;
    static ComVar<Boolean>          crownEventActive;
    static ComVar<Boolean>          crownEventIsTimed;
    static ComVar<Boolean>          hulkSmashPoles;
    static ComVar<Boolean>          endOpen;
    static ComVar<Boolean>          allowNonOwnerSwords;
    static ComVar<Boolean>          gb_extraExpGivesRhines;

    static ComVar<Integer>          effectCost_arrow;
    static ComVar<Integer>          effectCost_death;
    static ComVar<Integer>          effectCost_travel;
    static ComVar<Integer>          swordGoalGainPerKill;
    static ComVar<Integer>          houses_startingSupply;
    static ComVar<Integer>          tpTickDelay;
    static ComVar<Integer>          tpCooldown;
    static ComVar<Integer>          tpaExpiryTime;
    static ComVar<Integer>          startRhines;
    static ComVar<Integer>          baronPrice;
    static ComVar<Integer>          maxMoneyAmount;
    static ComVar<Integer>          maxTreasurePrize;
    static ComVar<Integer>          minTreasurePrize;
    static ComVar<Integer>          maxShopEarnings;
    static ComVar<Integer>          maxSignShopPrice;
    static ComVar<Integer>          defaultShopPrice;
    static ComVar<Integer>          guildPayIntervalDays;
    static ComVar<Integer>          guildBaseWage;

    private ComVars() {}

    private static ComVarJson j;
    public static void reload() {
        readJson();

        read("onFirstJoinKit",              ComVarTypes.KEY);

        read("regionWorld",                 ComVarTypes.WORLD);

        read("spawnRegion",                 ComVarTypes.STRING);
        read("discordLink",                 ComVarTypes.STRING);

        read("maxNickLength",               ComVarTypes.BYTE);
        read("maxBossDifficulty",           ComVarTypes.BYTE);
        read("maxTreasureItems",            ComVarTypes.BYTE);

        read("nearRadius",                  ComVarTypes.SHORT);
        read("hoppersInOneChunk",           ComVarTypes.SHORT);
        read("maxGuildMembers",             ComVarTypes.SHORT);

        read("houses_startingDemand",       ComVarTypes.FLOAT);
        read("guildWageModifier",           ComVarTypes.FLOAT);
        read("rwDoubleDropRate",            ComVarTypes.FLOAT);

        read("autoSaveIntervalMins",        ComVarTypes.LONG);

        read("resourceWorldResetInterval",  ComVarTypes.TIME);
        read("marriageCooldown",            ComVarTypes.TIME);
        read("userDataResetInterval",       ComVarTypes.TIME);
        read("marketOwnershipSafeTime",     ComVarTypes.TIME);
        read("evictionCleanupTime",         ComVarTypes.TIME);
        read("voteTime",                    ComVarTypes.TIME);
        read("voteInterval",                ComVarTypes.TIME);
        read("guildJoinRequirement",        ComVarTypes.TIME);
        read("marketStatusCooldown",        ComVarTypes.TIME);
        read("afkKickDelay",                ComVarTypes.TIME);
        read("dataRetentionTime",           ComVarTypes.TIME);
        read("guildJoinTime",               ComVarTypes.TIME);
        read("gb_donorTimeLength",          ComVarTypes.TIME);

        read("allowOtherPlayerNicks",       ComVarTypes.BOOL);
        read("taxesEnabled",                ComVarTypes.BOOL);
        read("logAdminShop",                ComVarTypes.BOOL);
        read("logNormalShop",               ComVarTypes.BOOL);
        read("crownEventActive",            ComVarTypes.BOOL);
        read("crownEventIsTimed",           ComVarTypes.BOOL);
        read("hulkSmashPoles",              ComVarTypes.BOOL);
        read("endOpen",                     ComVarTypes.BOOL);
        read("allowNonOwnerSwords",         ComVarTypes.BOOL);
        read("gb_extraExpGivesRhines",      ComVarTypes.BOOL);

        read("effectCost_arrow",            ComVarTypes.INTEGER);
        read("effectCost_death",            ComVarTypes.INTEGER);
        read("effectCost_travel",           ComVarTypes.INTEGER);
        read("swordGoalGainPerKill",        ComVarTypes.INTEGER);
        read("houses_startingSupply",       ComVarTypes.INTEGER);
        read("tpTickDelay",                 ComVarTypes.INTEGER);
        read("tpCooldown",                  ComVarTypes.INTEGER);
        read("tpaExpiryTime",               ComVarTypes.INTEGER);
        read("startRhines",                 ComVarTypes.INTEGER);
        read("baronPrice",                  ComVarTypes.INTEGER);
        read("maxMoneyAmount",              ComVarTypes.INTEGER);
        read("maxTreasurePrize",            ComVarTypes.INTEGER);
        read("minTreasurePrize",            ComVarTypes.INTEGER);
        read("maxShopEarnings",             ComVarTypes.INTEGER);
        read("maxSignShopPrice",            ComVarTypes.INTEGER);
        read("defaultShopPrice",            ComVarTypes.INTEGER);
        read("guildPayIntervalDays",        ComVarTypes.INTEGER);
        read("guildBaseWage",               ComVarTypes.INTEGER);

        j = null;
    }

    static void ensureDefaultsExist() {
        try {
            FileDefaults.JSON.compareAndSave(getFile(), Crown.resource("comvars.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        ComVarJson json = createJson();

        //Fuck you, I cannot be bothered to write all the variables again
        for (Field f: ComVars.class.getDeclaredFields()) {
            if(f.getType() != ComVar.class) continue;

            try {
                json.addVar((ComVar) f.get(null));
            } catch (Exception e) {
                Crown.logger().error("Could not save comvar: " + f.getName(), e);
            }
        }

        writeJson(json);
    }

    private static void read(String name, ComVarType type) {
        try {
            Field f = ComVars.class.getDeclaredField(name);
            f.set(null, j.getVar(name, type));
        } catch (Exception e) {
            Crown.logger().error("Could not read comvar: " + name, e);
        }
    }

    private static ComVarJson createJson() {
        return new ComVarJson(new JsonObject());
    }

    private static void readJson() {
        File f = getFile();
        try {
            j = new ComVarJson(JsonUtils.readFileObject(f));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read JSON from ComVar file", e);
        }
    }

    private static void writeJson(ComVarJson json) {
        try {
            JsonUtils.writeFile(json.getSource(), getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getFile() {
        File file = new File(Crown.dataFolder(), "comvars.json");

        if(file.isDirectory()) file.delete();
        if(!file.exists()) Crown.saveResource(true, "comvars.json");

        return file;
    }

    public static String getSpawnRegion() {
        return spawnRegion.getValue(RegionConstants.DEFAULT_SPAWN_NAME);
    }

    public static String discordLink() {
        return discordLink.getValue("https://discord.gg/wXjHNdp");
    }

    public static World getRegionWorld() {
        return regionWorld.getValue(Worlds.OVERWORLD);
    }


    public static byte getMaxBossDifficulty() {
        return maxBossDifficulty.getValue((byte) 5);
    }

    public static byte getMaxTreasureItems() {
        return maxTreasureItems.getValue((byte) 5);
    }

    public static byte getMaxNickLength() {
        return maxNickLength.getValue((byte) 16);
    }




    public static short getHoppersInOneChunk() {
        return hoppersInOneChunk.getValue((short) 256);
    }

    public static short getNearRadius() {
        return nearRadius.getValue((short) 200);
    }

    public static short getMaxGuildMembers() {
        return maxGuildMembers.getValue((short) 5);
    }



    public static float getHousesStartingDemand() {
        return houses_startingDemand.getValue(0F);
    }

    public static float guildWageModifier() {
        return guildWageModifier.getValue(0.25F);
    }

    public static float rwDoubleDropRate() {
        return rwDoubleDropRate.getValue(0.5F);
    }



    public static long getMarriageCooldown() {
        return marriageCooldown.getValue(259200000L);
    }

    public static long getUserResetInterval() {
        return userDataResetInterval.getValue(TimeUtil.MONTH_IN_MILLIS * 2);
    }

    public static long getShopOwnershipSafeTime() {
        return marketOwnershipSafeTime.getValue(TimeUtil.WEEK_IN_MILLIS * 2);
    }

    public static long getEvictionCleanupTime() {
        return evictionCleanupTime.getValue(TimeUtil.DAY_IN_MILLIS * 3);
    }

    public static long getVoteInterval() {
        return voteInterval.getValue(TimeUtil.DAY_IN_MILLIS * 3);
    }

    public static long getGuildJoinRequirement() {
        return guildJoinRequirement.getValue(TimeUtil.MONTH_IN_MILLIS * 2);
    }

    public static long getMarketStatusCooldown() {
        return marketStatusCooldown.getValue(TimeUtil.DAY_IN_MILLIS * 2);
    }

    public static long getVoteTime() {
        return voteTime.getValue(TimeUtil.DAY_IN_MILLIS * 3);
    }

    public static long resourceWorldResetInterval() {
        return resourceWorldResetInterval.getValue(TimeUtil.DAY_IN_MILLIS * 80);
    }

    public static long afkKickDelay() {
        return afkKickDelay.getValue(TimeUtil.HOUR_IN_MILLIS * 3);
    }

    public static long dataRetentionTime() {
        return dataRetentionTime.getValue(TimeUtil.MONTH_IN_MILLIS);
    }

    public static long guildKickSafeTime() {
        return guildJoinTime.getValue(TimeUtil.WEEK_IN_MILLIS);
    }

    public static long goalBookDonorTimeLength() {
        return gb_donorTimeLength.getValue(TimeUtil.MONTH_IN_MILLIS * 3);
    }



    public static boolean logAdminShopUsage() {
        return logAdminShop.getValue(true);
    }

    public static boolean areTaxesEnabled() {
        return taxesEnabled.getValue(false);
    }

    public static boolean logNormalShopUsage() {
        return logNormalShop.getValue(false);
    }

    public static boolean allowOtherPlayerNameNicks() {
        return allowOtherPlayerNicks.getValue(false);
    }

    public static boolean isEventActive() {
        return crownEventActive.getValue(false);
    }

    public static boolean isEventTimed() {
        return crownEventIsTimed.getValue(false);
    }

    public static boolean shouldHulkSmashPoles()  {
        return hulkSmashPoles.getValue(true);
    }

    public static boolean isEndOpen()  {
        return endOpen.getValue(false);
    }

    public static boolean allowNonOwnerSwords() {
        return allowNonOwnerSwords.getValue(true);
    }

    public static boolean goalBookExtraExpGivesRhines() {
        return gb_extraExpGivesRhines.getValue(true);
    }




    public static int arrowEffectCost() {
        return effectCost_arrow.getValue(1000);
    }

    public static int deathEffectCost() {
        return effectCost_death.getValue(2000);
    }

    public static int travelEffectCost() {
        return effectCost_travel.getValue(2500);
    }

    public static int swordGoalGainPerKill() {
        return swordGoalGainPerKill.getValue(1);
    }

    public static int getHousesStartingSupply() {
        return houses_startingSupply.getValue(250);
    }

    public static int getTpTickDelay() {
        return tpTickDelay.getValue(60);
    }

    public static int getTpCooldown() {
        return tpCooldown.getValue(60);
    }

    public static int getTpaExpiryTime() {
        return tpaExpiryTime.getValue(2400);
    }

    public static int getStartRhines() {
        return startRhines.getValue(100);
    }

    public static int getBaronPrice() {
        return baronPrice.getValue(500000);
    }

    public static int getMaxMoneyAmount() {
        return maxMoneyAmount.getValue(50000000);
    }

    public static int getTreasureMaxPrize() {
        return maxTreasurePrize.getValue(50000);
    }

    public static int getTreasureMinPrize() {
        return minTreasurePrize.getValue(10000);
    }

    public static int getMaxShopEarnings()  {
        return maxShopEarnings.getValue(500000);
    }

    public static int getMaxSignShopPrice()  {
        return maxSignShopPrice.getValue(1000000);
    }

    public static int defaultShopPrice() {
        return defaultShopPrice.getValue(55000);
    }

    public static int guildPayIntervalDays() {
        return guildPayIntervalDays.getValue(14);
    }

    public static int guildBaseWage() {
        return guildBaseWage.getValue(25000);
    }

    public static Key onFirstJoinKit() {
        return onFirstJoinKit.getValue();
    }

    private static class ComVarJson extends JsonWrapper {
        private ComVarJson(JsonObject json) {
            super(json);
        }

        public void addVar(ComVar var) {
            add(var.getName(), var);
        }

        public <T> ComVar<T> getVar(String name, ComVarType<T> type) {
            return ComVarRegistry.set(name, type, get(name));
        }
    }
}
