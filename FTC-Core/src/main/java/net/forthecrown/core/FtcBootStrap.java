package net.forthecrown.core;

import net.forthecrown.commands.CommandArkBox;
import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.core.admin.FtcPunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.FtcJailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.kingship.FtcKingship;
import net.forthecrown.core.transformers.CorrectLegacyData;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.economy.FtcEconomy;
import net.forthecrown.economy.ServerItemPriceMap;
import net.forthecrown.economy.guild.HazelguardTradersGuild;
import net.forthecrown.economy.guild.topics.VoteTopics;
import net.forthecrown.economy.houses.Houses;
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
import net.forthecrown.user.manager.FtcUserManager;
import net.forthecrown.user.packets.listeners.CorePacketListeners;

import static net.forthecrown.core.Main.*;
import static net.forthecrown.utils.FtcUtils.safeRunnable;

/**
 * A class which loads and creates everything the FTC plugin does and needs.
 * Aka a class which starts the plugin
 */
final class FtcBootStrap {
    private FtcBootStrap() {}

    static void firstPhase() {
        announcer = new FtcAnnouncer();

        messages = new FtcMessages();
        messages.load();

        emotes = new ChatEmotes();
        emotes.registerEmotes();

        prices = new ServerItemPriceMap();
        tabList = new FtcTabList();

        FtcFlags.init();
    }

    static void secondPhase() {
        joinInfo = new JoinInfo();

        economy = new FtcEconomy();
        regionManager = new FtcRegionManager(ComVars.getRegionWorld());

        userManager = new FtcUserManager();
        shopManager = new FtcShopManager();
        punishmentManager = new FtcPunishmentManager();
        usablesManager = new FtcUsablesManager();
        jailManager = new FtcJailManager();
        structureManager = new FtcStructureManager();

        kingship = new FtcKingship();
        rules = new ServerRules();

        markets = new FtcMarkets();
        tradersGuild = new HazelguardTradersGuild();

        //Initialize modules
        safeRunnable(CorePacketListeners::init);
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

        CorrectLegacyData.runAsync();

        //These must be last, since usage actions and checks are registered before them
        warpRegistry = new FtcWarpManager();
        kitRegistry = new FtcKitManager();

        Registries.COMVAR_TYPES.close();

        BannedWords.loadFromResource();
    }
}
