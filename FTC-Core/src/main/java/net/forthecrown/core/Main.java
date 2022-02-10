package net.forthecrown.core;

import net.forthecrown.commands.CommandArkBox;
import net.forthecrown.comvars.ComVarRegistry;
import net.forthecrown.comvars.types.ComVarTypes;
import net.forthecrown.core.admin.FtcJailManager;
import net.forthecrown.core.admin.FtcPunishments;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.goalbook.GoalBookImpl;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.economy.FtcEconomy;
import net.forthecrown.economy.ServerItemPriceMap;
import net.forthecrown.economy.guilds.TradeGuild;
import net.forthecrown.economy.market.FtcMarkets;
import net.forthecrown.economy.shops.FtcShopManager;
import net.forthecrown.events.MobHealthBar;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.regions.FtcRegionManager;
import net.forthecrown.structure.FtcStructureManager;
import net.forthecrown.structure.tree.test.TestNodes;
import net.forthecrown.useables.FtcUsablesManager;
import net.forthecrown.useables.kits.FtcKitManager;
import net.forthecrown.useables.warps.FtcWarpManager;
import net.forthecrown.user.FtcUserManager;
import net.forthecrown.user.packets.PacketListeners;
import net.forthecrown.utils.world.WorldLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Nameable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.dynmap.DynmapCommonAPIListener;

import static net.forthecrown.utils.FtcUtils.safeRunnable;

/**
 * Main class that does all the dirty internal stuff
 */
public final class Main extends JavaPlugin implements Crown {
    public static final String
            NAME            = "ForTheCrown",
            NAMESPACE       = NAME.toLowerCase(),
            OLD_NAMESPACE   = "ftccore";

    static Main                     inst;
    static Logger                   logger;

    static FtcEconomy               economy;
    static FtcAnnouncer             announcer;
    static FtcKingship              kingship;
    static FtcTabList               tabList;
    static FtcMarkets               markets;
    static FtcConfigImpl            config;

    static FtcUserManager           userManager;
    static FtcRegionManager         regionManager;
    static FtcUsablesManager        usablesManager;
    static FtcShopManager           shopManager;
    static FtcPunishments           punishmentManager;
    static FtcJailManager           jailManager;
    static FtcWarpManager           warpRegistry;
    static FtcKitManager            kitRegistry;
    static FtcStructureManager      structureManager;

    static ResourceWorld            resourceWorld;
    static GoalBookImpl             goalBook;
    static FtcMessages              messages;
    static ServerRules              rules;
    static ServerItemPriceMap       prices;
    static JoinInfo                 joinInfo;
    static ChatEmotes               emotes;
    static DayChange                dayChange;
    static TradeGuild               guild;
    static PeriodicalSaver          saver;
    static EndOpener                endOpener;

    static LuckPerms                luckPerms;

    @Override
    public void onEnable() {
        luckPerms = LuckPermsProvider.get();

        FtcBootStrap.enableBootStrap();

        announcer.doBroadcasts();
        dayChange.schedule();

        saverLogic();

        if(Crown.inDebugMode()) {
            TestNodes.init();
        }

        logger.info("FTC startup completed");
    }

    @Override
    public void onLoad() {
        VanillaChanges.softerDeepslate();
        inst = this;

        DynmapCommonAPIListener.register(new FtcDynmap());

        //Hacky way of determining if we're on the test server or not
        config = new FtcConfigImpl();
        config.ensureDefaultsExist();
        config.read();

        ComVars.inDebugMode = ComVarRegistry.set("debugMode", ComVarTypes.BOOL, config.getJson().getBool("debug_mode", false));

        logger = getLog4JLogger();

        saveResource("banned_words.json", true);

        RoyalCommandException.ENABLE_HOVER_STACK_TRACE = Crown.inDebugMode();
        FtcBootStrap.loadBootStrap();
    }

    @Override
    public void onDisable() {
        Crown.saveFTC();

        Bukkit.getScheduler().cancelTasks(this);

        for (Player p: Bukkit.getOnlinePlayers()) p.closeInventory();

        //Give all the entities their names back
        MobHealthBar.NAMES.forEach(Nameable::customName);

        safeRunnable(Bosses::shutDown);
        safeRunnable(Cosmetics::shutDown);
        safeRunnable(PacketListeners::removeAll);
        safeRunnable(CommandArkBox::save);
        safeRunnable(WorldLoader::shutdown);

        FtcUserManager.LOADED_USERS.clear();
        FtcUserManager.LOADED_ALTS.clear();
    }

    // IDK why this method has logic in the name, when there's
    // clearly no logic being used here, it's dumb af
    void saverLogic() {
        boolean savePeriodically = config.getJson().getBool("save_periodically");

        if(savePeriodically) {
            saver.start();
        } else {
            saver.cancel();
        }
    }

    @Override
    public @NonNull String namespace() {
        return NAMESPACE;
    }
}
