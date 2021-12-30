package net.forthecrown.core;

import net.forthecrown.commands.CommandArkBox;
import net.forthecrown.comvars.ComVar;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.comvars.types.ComVarTypes;
import net.forthecrown.core.admin.FtcPunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.FtcJailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.kingship.FtcKingship;
import net.forthecrown.core.transformers.NamespaceRenamer;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.crownevents.ArmorStandLeaderboard;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.economy.FtcEconomy;
import net.forthecrown.economy.ServerItemPriceMap;
import net.forthecrown.economy.guild.HazelguardTradersGuild;
import net.forthecrown.economy.market.FtcMarkets;
import net.forthecrown.economy.shops.FtcShopManager;
import net.forthecrown.events.MobHealthBar;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.regions.FtcRegionManager;
import net.forthecrown.structure.FtcStructureManager;
import net.forthecrown.useables.FtcUsablesManager;
import net.forthecrown.useables.kits.FtcKitManager;
import net.forthecrown.useables.warps.FtcWarpManager;
import net.forthecrown.user.manager.FtcUserManager;
import net.forthecrown.user.packets.PacketListeners;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dynmap.DynmapCommonAPIListener;

import java.io.File;
import java.util.logging.Logger;

import static net.forthecrown.utils.FtcUtils.safeRunnable;

/**
 * Main class that does all the dirty internal stuff
 */
public final class Main extends JavaPlugin implements Crown {
    public static final String NAME = "ForTheCrown";

    static ComVar<Boolean>          inDebugMode;

    static Main                     inst;

    static Location                 serverSpawn;
    static Logger                   logger;
    static String                   prefix = "&6[FTC]&r  ";
    static String                   discord;

    static FtcEconomy               economy;
    static FtcAnnouncer             announcer;
    static FtcKingship              kingship;

    static FtcUserManager           userManager;
    static FtcRegionManager         regionManager;
    static FtcUsablesManager        usablesManager;
    static FtcShopManager           shopManager;
    static FtcPunishmentManager     punishmentManager;
    static FtcJailManager           jailManager;
    static FtcWarpManager           warpRegistry;
    static FtcKitManager            kitRegistry;
    static FtcStructureManager      structureManager;

    static FtcMessages              messages;
    static FtcTabList               tabList;
    static ServerRules              rules;
    static ServerItemPriceMap       prices;
    static JoinInfo                 joinInfo;
    static ChatEmotes               emotes;
    static DayUpdate                dayUpdate;
    static FtcMarkets               markets;
    static HazelguardTradersGuild   tradersGuild;
    static PeriodicalSaver          saver;

    static LuckPerms                luckPerms;

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        FtcBootStrap.secondPhase();

        announcer.doBroadcasts();
        dayUpdate.checkDay();

        logger.info("FTC startup completed");

        /*if(RwResetter.shouldReset()) {
            RwResetter.reset();
        }*/
    }

    @Override
    public void onLoad() {
        NamespaceRenamer.run(getLogger(), getDataFolder());

        VanillaChanges.softerDeepslate();
        inst = this;

        DynmapCommonAPIListener.register(new FtcDynmap());

        //Hacky way of determining if we're on the test server or not
        inDebugMode = ComVarRegistry.set("debugMode", ComVarTypes.BOOL, !new File("plugins/CoreProtect/config.yml").exists());

        logger = getLogger();

        saveResource("banned_words.json", true);

        RoyalCommandException.ENABLE_HOVER_STACK_TRACE = Crown.inDebugMode();
        FtcBootStrap.firstPhase();
    }

    @Override
    public void onDisable() {
        Crown.saveFTC();

        Bukkit.getScheduler().cancelTasks(this);

        for (Player p: Bukkit.getOnlinePlayers()) p.closeInventory();
        ArmorStandLeaderboard.destroyAll();

        //Give all the entities their names back
        MobHealthBar.NAMES.forEach(Nameable::customName);

        safeRunnable(Bosses::shutDown);
        safeRunnable(Cosmetics::shutDown);
        safeRunnable(PacketListeners::removeAll);
        safeRunnable(CommandArkBox::save);

        FtcUserManager.LOADED_USERS.clear();
        FtcUserManager.LOADED_ALTS.clear();
    }

    @Override
    public void saveConfig() {
        FileConfiguration config = getConfig();
        config.set("ServerSpawn", serverSpawn);

        super.saveConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Configuration config = getConfig();

        prefix = config.getString("Prefix");
        discord = config.getString("Discord");

        ComVars.reload();

        Location defSpawnLoc = new Location(Worlds.VOID, 153.5, 5, 353.5, 90, 0);
        serverSpawn = config.getLocation("ServerSpawn", defSpawnLoc);
        dayUpdate = new DayUpdate((byte) config.getInt("Day"));

        if(saver != null){
            try {
                saver.cancel();
            } catch (IllegalStateException ignored){}
        }

        if(config.getBoolean("System.save-periodically")) {
            saver = new PeriodicalSaver(this);
            saver.start();
        } else if (saver != null && !saver.isCancelled()){
            saver.cancel();
            saver = null;
        }
    }

    @Override
    public @NonNull String namespace() {
        return NAME.toLowerCase();
    }
}
