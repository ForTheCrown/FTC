package net.forthecrown.core;

import net.forthecrown.commands.CommandArkBox;
import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.core.admin.FtcPunisher;
import net.forthecrown.core.battlepass.BattlePassImpl;
import net.forthecrown.core.chat.*;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.boss.evoker.EvokerVars;
import net.forthecrown.dungeons.level.LevelSerializer;
import net.forthecrown.economy.FtcEconomy;
import net.forthecrown.economy.ServerItemPriceMap;
import net.forthecrown.economy.guilds.TradeGuild;
import net.forthecrown.economy.guilds.topics.VoteTopics;
import net.forthecrown.economy.houses.Houses;
import net.forthecrown.economy.houses.Properties;
import net.forthecrown.economy.market.FtcMarkets;
import net.forthecrown.economy.shops.FtcShopManager;
import net.forthecrown.events.Events;
import net.forthecrown.inventory.crown.Crowns;
import net.forthecrown.inventory.weapon.RoyalWeapons;
import net.forthecrown.regions.FtcRegionManager;
import net.forthecrown.registry.Registries;
import net.forthecrown.structure.FtcStructureManager;
import net.forthecrown.useables.FtcUsablesManager;
import net.forthecrown.useables.actions.UsageActions;
import net.forthecrown.useables.checks.UsageChecks;
import net.forthecrown.useables.kits.FtcKitManager;
import net.forthecrown.useables.warps.FtcWarpManager;
import net.forthecrown.user.FtcUserManager;
import net.forthecrown.user.packets.listeners.CorePacketListeners;
import net.forthecrown.vars.types.VarTypes;

import static net.forthecrown.core.Main.*;
import static net.forthecrown.utils.FtcUtils.safeRunnable;
import static net.forthecrown.vars.VarRegistry.def;

/**
 * A class which loads and creates everything the FTC plugin does and needs.
 * Aka a class which starts the plugin
 */
final class BootStrap {
    private BootStrap() {}

    static void loadPhase() {
        announcer   = new FtcAnnouncer();
        messages    = new FtcMessages();
        emotes      = new ChatEmotes();
        prices      = new ServerItemPriceMap();
        tabList     = new FtcTabList();
        userManager = new FtcUserManager();

        messages.load();
        emotes.registerEmotes();

        FtcFlags.init();
    }

    static void enablePhase() {
        FtcVars.regionWorld = def("regionWorld", VarTypes.WORLD, Worlds.OVERWORLD);

        // Initialize config sections
        joinInfo        = new JoinInfo();
        dayChange       = new DayChange();
        kingship        = new FtcKingship();
        rules           = new ServerRules();
        endOpener       = new EndOpener();
        resourceWorld   = new ResourceWorld();

        // Add config sections
        config.addSection(endOpener);
        config.addSection(resourceWorld);
        config.addSection(joinInfo);
        config.addSection(kingship);
        config.addSection(rules);
        config.addSection(tabList);

        // Register day change listeners
        dayChange.addListener(endOpener);
        dayChange.addListener(resourceWorld);

        // Only load here, cuz we've already read the JSON
        config.load();

        economy = new FtcEconomy();

        shopManager         = new FtcShopManager();
        regionManager       = new FtcRegionManager(FtcVars.getRegionWorld());
        usablesManager      = new FtcUsablesManager();
        structureManager    = new FtcStructureManager();

        punisher    = new FtcPunisher();
        markets     = new FtcMarkets();
        saver       = new PeriodicalSaver();
        guild       = new TradeGuild();
        battlePass  = new BattlePassImpl();

        //Initialize modules
        safeRunnable(CorePacketListeners::init);
        safeRunnable(Properties::init);
        safeRunnable(Houses::init);
        safeRunnable(EvokerVars::init);
        safeRunnable(Bosses::init);
        safeRunnable(LevelSerializer::load);
        safeRunnable(RoyalWeapons::init);
        safeRunnable(Crowns::init);
        safeRunnable(Cosmetics::init);
        safeRunnable(UsageChecks::init);
        safeRunnable(UsageActions::init);
        safeRunnable(VoteTopics::init);
        safeRunnable(FtcCommands::init);
        safeRunnable(Events::init);
        safeRunnable(CommandArkBox::load);

        //These must be last, since usage actions and checks are registered before them
        warpRegistry = new FtcWarpManager();
        kitRegistry = new FtcKitManager();

        Registries.VAR_TYPES.close();

        BannedWords.loadFromResource();
    }
}