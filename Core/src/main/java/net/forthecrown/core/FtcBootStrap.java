package net.forthecrown.core;

import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.core.admin.FtcPunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.FtcJailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.kingship.FtcKingship;
import net.forthecrown.core.transformers.Homes_PopDensityToFTC;
import net.forthecrown.core.transformers.Regions_PopDensityToFTC;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.economy.FtcEconomy;
import net.forthecrown.economy.ServerItemPriceMap;
import net.forthecrown.economy.market.FtcMarketRegion;
import net.forthecrown.economy.market.guild.HazelguardTradersGuild;
import net.forthecrown.economy.market.guild.topics.VoteTopics;
import net.forthecrown.economy.shops.FtcShopManager;
import net.forthecrown.events.Events;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.regions.FtcRegionManager;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.useables.FtcUsablesManager;
import net.forthecrown.useables.actions.UsageActions;
import net.forthecrown.useables.checks.UsageChecks;
import net.forthecrown.useables.kits.FtcKitManager;
import net.forthecrown.useables.warps.FtcWarpManager;
import net.forthecrown.user.manager.FtcUserManager;
import net.forthecrown.utils.Worlds;

import static net.forthecrown.utils.FtcUtils.safeRunnable;

/**
 * A class which loads and creates everything the FTC plugin does and needs.
 * Aka a class which starts the plugin
 */
public final class FtcBootStrap {
    private FtcBootStrap() {}

    static void firstPhase() {
        Main.announcer = new FtcAnnouncer();

        Main.messages = new FtcMessages();
        Main.messages.load();

        Main.emotes = new ChatEmotes();
        Main.emotes.registerEmotes();

        Main.prices = new ServerItemPriceMap();
        Main.tabList = new FtcTabList();

        FtcFlags.init();
    }

    static void secondPhase() {
        Main.joinInfo = new JoinInfo();

        Main.userSerializer = new UserJsonSerializer();
        Main.economy = new FtcEconomy();
        Main.regionManager = new FtcRegionManager(Worlds.OVERWORLD);

        Regions_PopDensityToFTC.checkAndRun();
        Homes_PopDensityToFTC.checkAndRun();

        //Instantiate managers
        Main.userManager = new FtcUserManager();
        Main.shopManager = new FtcShopManager();
        Main.punishmentManager = new FtcPunishmentManager();
        Main.usablesManager = new FtcUsablesManager();
        Main.jailManager = new FtcJailManager();

        //Instantiate these things :shrug:
        Main.marketRegion = new FtcMarketRegion();
        Main.kingship = new FtcKingship();
        Main.rules = new ServerRules();
        Main.tradersGuild = new HazelguardTradersGuild();

        //Initialize modules
        safeRunnable(Pirates::init);
        safeRunnable(Bosses::init);
        safeRunnable(Cosmetics::init);
        safeRunnable(UsageChecks::init);
        safeRunnable(UsageActions::init);
        safeRunnable(VoteTopics::init);
        safeRunnable(FtcCommands::init);
        safeRunnable(Events::init);

        //These must be last, since usage actions and checks are registered before them
        Main.warpRegistry = new FtcWarpManager();
        Main.kitRegistry = new FtcKitManager();

        Registries.COMVAR_TYPES.close();

        BannedWords.loadFromResource();
    }
}
