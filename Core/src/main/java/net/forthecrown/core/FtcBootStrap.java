package net.forthecrown.core;

import net.forthecrown.commands.manager.FtcCommands;
import net.forthecrown.core.admin.CrownPunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.FtcJailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.kingship.CrownKingship;
import net.forthecrown.core.transformers.Balances_YamlToJson;
import net.forthecrown.core.transformers.Regions_PopDensityToFTC;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.economy.CrownBalances;
import net.forthecrown.economy.ServerItemPriceMap;
import net.forthecrown.economy.market.FtcMarketRegion;
import net.forthecrown.economy.market.guild.HazelguardTradersGuild;
import net.forthecrown.economy.shops.CrownShopManager;
import net.forthecrown.economy.shops.template.ShopTemplates;
import net.forthecrown.events.Events;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.regions.FtcRegionManager;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.useables.CrownUsablesManager;
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

        Main.messages = new CrownMessages();
        Main.messages.load();

        Main.emotes = new ChatEmotes();
        Main.emotes.registerEmotes();

        Main.prices = new ServerItemPriceMap();
        Main.tabList = new CrownTabList();

        WgFlags.init();
    }

    static void secondPhase() {
        Main.joinInfo = new JoinInfo();

        Main.userSerializer = new UserJsonSerializer();

        Balances_YamlToJson.checkAndRun();
        Main.balances = new CrownBalances();

        Main.regionManager = new FtcRegionManager(Worlds.OVERWORLD);
        Regions_PopDensityToFTC.checkAndRun();

        //Instantiate default shop templates
        safeRunnable(ShopTemplates::init);

        //Instantiate managers
        Main.userManager = new FtcUserManager();
        Main.shopManager = new CrownShopManager();
        Main.punishmentManager = new CrownPunishmentManager();
        Main.usablesManager = new CrownUsablesManager();
        Main.jailManager = new FtcJailManager();

        //Instantiate these things :shrug:
        Main.marketShops = new FtcMarketRegion();
        Main.kingship = new CrownKingship();
        Main.rules = new ServerRules();
        Main.tradersGuild = new HazelguardTradersGuild();

        //Initialize modules
        safeRunnable(Pirates::init);
        safeRunnable(Bosses::init);
        safeRunnable(Cosmetics::init);
        safeRunnable(UsageChecks::init);
        safeRunnable(UsageActions::init);
        safeRunnable(FtcCommands::init);
        safeRunnable(Events::init);

        //These must be last, since usage actions and checks are registered before them
        Main.warpRegistry = new FtcWarpManager();
        Main.kitRegistry = new FtcKitManager();

        Registries.COMVAR_TYPES.close();
    }
}
