package net.forthecrown.core;

import net.forthecrown.commands.manager.CoreCommands;
import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.comvars.types.ComVarType;
import net.forthecrown.core.admin.CrownPunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.CrownJailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.kingship.CrownKingship;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.crownevents.ArmorStandLeaderboard;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.economy.CrownBalances;
import net.forthecrown.economy.ServerItemPriceMap;
import net.forthecrown.economy.shops.CrownShopManager;
import net.forthecrown.events.Events;
import net.forthecrown.events.MobHealthBar;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.registry.CrownKitRegistry;
import net.forthecrown.registry.CrownWarpRegistry;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.useables.CrownUsablesManager;
import net.forthecrown.useables.actions.UsageActions;
import net.forthecrown.useables.preconditions.UsageChecks;
import net.forthecrown.user.CrownUserManager;
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
public final class Main extends JavaPlugin implements CrownCore {

    //Hacky way of determining if we're on the test server or not
    public static ComVar<Boolean> inDebugMode;

    static Main                     inst;

    static String                   prefix = "&6[FTC]&r  ";
    static String                   discord;
    static PeriodicalSaver          saver;

    static CrownBalances            balances;
    static CrownBroadcaster         announcer;
    static CrownKingship            kingship;
    static UserJsonSerializer       userSerializer;

    static CrownUsablesManager      usablesManager;
    static CrownShopManager         shopManager;
    static CrownPunishmentManager   punishmentManager;
    static CrownUserManager         userManager;
    static CrownJailManager         jailManager;

    static CrownWarpRegistry        warpRegistry;
    static CrownKitRegistry         kitRegistry;

    static Location                 serverSpawn;
    static CrownMessages            messages;
    static CrownTabList             tabList;
    static ServerRules              rules;
    static ServerItemPriceMap       prices;
    static JoinInfo                 joinInfo;
    static ChatEmotes               emotes;
    static DayUpdate                dayUpdate;
    static Logger                   logger;

    public static final Set<ArmorStandLeaderboard> LEADERBOARDS = new HashSet<>();
    static LuckPerms luckPerms;

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        joinInfo = new JoinInfo();

        balances = new CrownBalances();
        userSerializer = new UserJsonSerializer();

        shopManager = new CrownShopManager();
        userManager = new CrownUserManager();
        punishmentManager = new CrownPunishmentManager();
        jailManager = new CrownJailManager();
        kingship = new CrownKingship();
        rules = new ServerRules();

        usablesManager = new CrownUsablesManager();

        safeRunnable(Pirates::init);
        safeRunnable(Bosses::init);
        safeRunnable(Cosmetics::init);

        safeRunnable(UsageChecks::init);
        safeRunnable(UsageActions::init);

        warpRegistry = new CrownWarpRegistry();
        kitRegistry = new CrownKitRegistry();

        announcer.doBroadcasts();

        safeRunnable(CoreCommands::init);
        safeRunnable(Events::init);

        dayUpdate.checkDay();

        if(getConfig().getBoolean("System.run-deleter-on-startup")) userManager.runUserDeletionCheck();

        Registries.COMVAR_TYPES.close();

        logger.info("FTC startup completed");
    }

    @Override
    public void onLoad() {
        inst = this;
        inDebugMode = ComVarRegistry.set("core_debug", ComVarType.BOOLEAN, !new File("plugins/CoreProtect/config.yml").exists());

        logger = getLogger();
        announcer = new CrownBroadcaster();

        messages = new CrownMessages();
        messages.load();

        emotes = new ChatEmotes();
        emotes.registerEmotes();

        prices = new ServerItemPriceMap();
        tabList = new CrownTabList();

        WgFlags.init();
    }

    @Override
    public void onDisable() {
        if(getConfig().getBoolean("System.save-on-disable")) CrownCore.saveFTC();

        Bukkit.getScheduler().cancelTasks(this);

        for (Player p: Bukkit.getOnlinePlayers()) p.closeInventory();
        for (ArmorStandLeaderboard a: LEADERBOARDS) a.destroy();
        for (LivingEntity e: MobHealthBar.NAMES.keySet()) e.customName(MobHealthBar.NAMES.getOrDefault(e, null));

        safeRunnable(Bosses::shutDown);
        safeRunnable(Cosmetics::shutDown);
        safeRunnable(Pirates::shutDown);

        CrownUserManager.LOADED_USERS.clear();
        CrownUserManager.LOADED_ALTS.clear();
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
