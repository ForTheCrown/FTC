package net.forthecrown.core;

import net.forthecrown.commands.CommandArkBox;
import net.forthecrown.core.admin.FtcPunisher;
import net.forthecrown.core.battlepass.BattlePassImpl;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.transformers.RegionResidencyTransformer;
import net.forthecrown.core.transformers.ShopJsonToTag;
import net.forthecrown.core.transformers.Transformers;
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
import net.forthecrown.vars.VarRegistry;
import net.forthecrown.vars.types.VarTypes;
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
    static FtcPunisher              punisher;
    static FtcWarpManager           warpRegistry;
    static FtcKitManager            kitRegistry;
    static FtcStructureManager      structureManager;

    static ResourceWorld            resourceWorld;
    static BattlePassImpl           battlePass;
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

        BootStrap.enablePhase();

        announcer.doBroadcasts();
        dayChange.schedule();
        shopManager.reload();

        saverLogic();

        if(Crown.inDebugMode()) {
            TestNodes.init();
        }

        if(ShopJsonToTag.shouldRun()) {
            ShopJsonToTag.run(shopManager);
        }

        if(RegionResidencyTransformer.shouldRun()) {
            RegionResidencyTransformer.run();
        }


        logger.info("FTC startup completed");
    }

    @Override
    public void onLoad() {
        // Set logger and instance
        logger = getLog4JLogger();
        inst = this;

        // Register dynmap hook connection thing
        DynmapCommonAPIListener.register(new FtcDynmap());

        // Create config
        config = new FtcConfigImpl();
        config.ensureDefaultsExist();
        config.read();

        // Set up Vars
        VarTypes.init();
        VarRegistry.load();
        FtcVars.inDebugMode = VarRegistry.set("debugMode", VarTypes.BOOL, config.getJson().getBool("debug_mode", false))
                .setTransient(true);

        saveResource("banned_words.json", true);

        RoyalCommandException.ENABLE_HOVER_STACK_TRACE = Crown.inDebugMode();
        BootStrap.loadPhase();

        userManager.loadCache();

        Transformers.load(getDataFolder());

        // Remove any potentially invalid user datas
        //InvalidUserDataFilter.run(UserJsonSerializer.USER_DIR, userManager);

        logger.info("onLoad finished");
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

        if(savePeriodically) saver.start();
        else saver.cancel();
    }

    @Override
    public @NonNull String namespace() {
        return NAMESPACE;
    }
}