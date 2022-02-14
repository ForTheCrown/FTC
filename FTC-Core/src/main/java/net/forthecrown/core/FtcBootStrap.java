package net.forthecrown.core;

import net.forthecrown.commands.CommandArkBox;
import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.core.admin.FtcJailManager;
import net.forthecrown.core.admin.FtcPunishments;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.chat.*;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.dungeons.Bosses;
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

import static net.forthecrown.core.Main.*;
import static net.forthecrown.utils.FtcUtils.safeRunnable;

/**
 * A class which loads and creates everything the FTC plugin does and needs.
 * Aka a class which starts the plugin
 */
final class FtcBootStrap {
    private FtcBootStrap() {}

    static void loadBootStrap() {
        announcer   = new FtcAnnouncer();
        messages    = new FtcMessages();
        emotes      = new ChatEmotes();
        prices      = new ServerItemPriceMap();
        tabList     = new FtcTabList();

        messages.load();
        emotes.registerEmotes();

        FtcFlags.init();

        ComVars.ensureDefaultsExist();
        ComVars.reload();
    }

    static void enableBootStrap() {
        // Initialize config sections
        joinInfo        = new JoinInfo();
        dayChange       = new DayChange();
        kingship        = new FtcKingship();
        rules           = new ServerRules();
        endOpener       = new EndOpener();
        resourceWorld   = new ResourceWorld();

        // Add config sections
        config.addSection(resourceWorld);
        config.addSection(endOpener);
        config.addSection(joinInfo);
        config.addSection(kingship);
        config.addSection(rules);
        config.addSection(tabList);

        // Register day change listeners
        dayChange.addListener(resourceWorld);
        dayChange.addListener(endOpener);

        // Only load here, cuz we've already read the JSON
        config.load();

        economy = new FtcEconomy();

        shopManager         = new FtcShopManager();
        regionManager       = new FtcRegionManager(ComVars.getRegionWorld());
        userManager         = new FtcUserManager();
        punishmentManager   = new FtcPunishments();
        jailManager         = new FtcJailManager();
        usablesManager      = new FtcUsablesManager();
        structureManager    = new FtcStructureManager();

        markets = new FtcMarkets();
        saver   = new PeriodicalSaver();
        guild   = new TradeGuild();

        //Initialize modules
        safeRunnable(CorePacketListeners::init);
        safeRunnable(Properties::init);
        safeRunnable(Houses::init);
        safeRunnable(Bosses::init);
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

        Registries.COMVAR_TYPES.close();

        BannedWords.loadFromResource();
    }
}
