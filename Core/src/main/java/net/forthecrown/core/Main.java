package net.forthecrown.core;

import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.comvars.types.ComVarTypes;
import net.forthecrown.core.admin.CrownPunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.FtcJailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.kingship.CrownKingship;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.crownevents.ArmorStandLeaderboard;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.economy.CrownBalances;
import net.forthecrown.economy.ServerItemPriceMap;
import net.forthecrown.economy.market.FtcMarketShops;
import net.forthecrown.economy.shops.CrownShopManager;
import net.forthecrown.events.MobHealthBar;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.regions.FtcRegionManager;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.useables.CrownUsablesManager;
import net.forthecrown.useables.kits.FtcKitManager;
import net.forthecrown.useables.warps.FtcWarpManager;
import net.forthecrown.user.manager.FtcUserManager;
import net.forthecrown.user.GameModePacketListener;
import net.forthecrown.utils.Worlds;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import static net.forthecrown.utils.FtcUtils.safeRunnable;

/**
 * Main class that does all the dirty internal stuff
 */
public final class Main extends JavaPlugin implements Crown {

    //Hacky way of determining if we're on the test server or not
    public static ComVar<Boolean> inDebugMode;

    static Main                     inst;

    static String                   prefix = "&6[FTC]&r  ";
    static String                   discord;
    static PeriodicalSaver          saver;

    static CrownBalances            balances;
    static FtcAnnouncer             announcer;
    static CrownKingship            kingship;
    static UserJsonSerializer       userSerializer;

    static FtcUserManager           userManager;
    static FtcRegionManager         regionManager;
    static CrownUsablesManager      usablesManager;
    static CrownShopManager         shopManager;
    static CrownPunishmentManager   punishmentManager;
    static FtcJailManager           jailManager;
    static FtcWarpManager           warpRegistry;
    static FtcKitManager            kitRegistry;

    static Location                 serverSpawn;
    static CrownMessages            messages;
    static CrownTabList             tabList;
    static ServerRules              rules;
    static ServerItemPriceMap       prices;
    static JoinInfo                 joinInfo;
    static ChatEmotes               emotes;
    static DayUpdate                dayUpdate;
    static FtcMarketShops           marketShops;
    static Logger                   logger;

    public static final Set<ArmorStandLeaderboard> LEADERBOARDS = new HashSet<>();
    static LuckPerms luckPerms;

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        FtcBootStrap.secondPhase();

        announcer.doBroadcasts();
        dayUpdate.checkDay();

        logger.info("FTC startup completed");
    }

    @Override
    public void onLoad() {
        inst = this;
        inDebugMode = ComVarRegistry.set("debugMode", ComVarTypes.BOOLEAN, !new File("plugins/CoreProtect/config.yml").exists());
        logger = getLogger();

        RoyalCommandException.ENABLE_HOVER_STACK_TRACE = Crown.inDebugMode();

        FtcBootStrap.firstPhase();
    }

    @Override
    public void onDisable() {
        if(getConfig().getBoolean("System.save-on-disable")) Crown.saveFTC();

        Bukkit.getScheduler().cancelTasks(this);

        for (Player p: Bukkit.getOnlinePlayers()) p.closeInventory();
        for (ArmorStandLeaderboard a: LEADERBOARDS) a.destroy();
        for (LivingEntity e: MobHealthBar.NAMES.keySet()) e.customName(MobHealthBar.NAMES.getOrDefault(e, null));

        safeRunnable(Bosses::shutDown);
        safeRunnable(Cosmetics::shutDown);
        safeRunnable(Pirates::shutDown);
        safeRunnable(GameModePacketListener::removeAll);

        /*if(DebugConstants.VIKINGS_ENABLED) {
            safeRunnable(ValhallaEngine::shutDown);
        }*/

        if(Crown.inDebugMode()) {
            safeRunnable(regionManager::save);
        }

        FtcUserManager.LOADED_USERS.clear();
        FtcUserManager.LOADED_ALTS.clear();
    }

    @Override
    public void saveConfig() {
        FileConfiguration config = getConfig();

        config.set("ServerSpawn", serverSpawn);

        ComVars.save(config);

        super.saveConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Configuration config = getConfig();

        prefix = config.getString("Prefix");
        discord = config.getString("Discord");

        ComVars.reload(config);

        Location defSpawnLoc = new Location(Worlds.VOID, 153.5, 5, 353.5, 90, 0);
        serverSpawn = config.getLocation("ServerSpawn", defSpawnLoc);

        dayUpdate = new DayUpdate((byte) config.getInt("Day"));

        if(saver != null){
            try {
                saver.cancel();
            } catch (IllegalStateException ignored){}
        }

        if(config.getBoolean("System.save-periodically")){
            saver = new PeriodicalSaver(this);
            saver.start();
        } else if (saver != null && !saver.isCancelled()){
            saver.cancel();
            saver = null;
        }
    }

    @Override
    public @NonNull String namespace() {
        return getName().toLowerCase();
    }
}
