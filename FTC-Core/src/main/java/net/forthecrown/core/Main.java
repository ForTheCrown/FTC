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
import org.bukkit.Nameable;
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
    public static final String
            NAME            = "ForTheCrown",
            NAMESPACE       = NAME.toLowerCase(),
            OLD_NAMESPACE   = "ftccore";

    static ComVar<Boolean>          inDebugMode;

    static Main                     inst;
    static Logger                   logger;

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
    static EndOpener                endOpener;

    static LuckPerms                luckPerms;
    static FtcConfigImpl            config;

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();

        FtcBootStrap.enableBootStrap();

        announcer.doBroadcasts();
        dayUpdate.schedule();

        saverLogic();

        /*if(RwResetter.shouldReset()) {
            RwResetter.reset();
        }*/

        logger.info("FTC startup completed");
    }

    @Override
    public void onLoad() {
        VanillaChanges.softerDeepslate();
        inst = this;

        DynmapCommonAPIListener.register(new FtcDynmap());

        //Hacky way of determining if we're on the test server or not
        inDebugMode = ComVarRegistry.set("debugMode", ComVarTypes.BOOL, !new File("plugins/CoreProtect/config.yml").exists());

        logger = getLogger();

        saveResource("banned_words.json", true);

        RoyalCommandException.ENABLE_HOVER_STACK_TRACE = Crown.inDebugMode();
        FtcBootStrap.loadBootStrap();
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

    // IDK why this method has logic in the name, when there's
    // clearly no logic being used here, it's dumb af
    void saverLogic() {
        if(saver != null){
            try {
                saver.cancel();
            } catch (IllegalStateException ignored){}
        }

        if(config.getJson().getBool("save_periodically")) {
            saver = new PeriodicalSaver(this);
            saver.start();
        } else if (saver != null && !saver.isCancelled()){
            saver.cancel();
            saver = null;
        }
    }

    @Override
    public @NonNull String namespace() {
        return NAMESPACE;
    }
}
