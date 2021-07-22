package net.forthecrown.core;

import net.forthecrown.commands.manager.CoreCommands;
import net.forthecrown.core.admin.CrownPunishmentManager;
import net.forthecrown.core.admin.ServerRules;
import net.forthecrown.core.admin.jails.CrownJailManager;
import net.forthecrown.core.chat.*;
import net.forthecrown.core.converters.Balances_YamlToJson;
import net.forthecrown.core.kingship.CrownKingship;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.economy.CrownBalances;
import net.forthecrown.economy.ServerItemPriceMap;
import net.forthecrown.economy.shops.CrownShopManager;
import net.forthecrown.events.Events;
import net.forthecrown.pirates.Pirates;
import net.forthecrown.registry.CrownKitRegistry;
import net.forthecrown.registry.CrownWarpRegistry;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.useables.CrownUsablesManager;
import net.forthecrown.useables.actions.UsageActions;
import net.forthecrown.useables.preconditions.UsageChecks;
import net.forthecrown.user.CrownUserManager;
import net.forthecrown.valhalla.ValhallaEngine;

import static net.forthecrown.utils.FtcUtils.safeRunnable;

/**
 * A class which loads and creates everything the FTC plugin does and needs.
 * Aka a class which starts the plugin
 */
public final class FtcBootStrap {
    private FtcBootStrap() {}

    static void firstPhase() {
        Main.announcer = new CrownBroadcaster();

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

        Balances_YamlToJson.checkAndRun();
        Main.balances = new CrownBalances();

        Main.userSerializer = new UserJsonSerializer();

        Main.shopManager = new CrownShopManager();
        Main.userManager = new CrownUserManager();
        Main.punishmentManager = new CrownPunishmentManager();
        Main.usablesManager = new CrownUsablesManager();
        Main.jailManager = new CrownJailManager();

        Main.kingship = new CrownKingship();
        Main.rules = new ServerRules();

        safeRunnable(Pirates::init);
        safeRunnable(Bosses::init);
        safeRunnable(Cosmetics::init);
        if(CrownCore.inDebugMode()) safeRunnable(ValhallaEngine::init);
        safeRunnable(UsageChecks::init);
        safeRunnable(UsageActions::init);
        safeRunnable(CoreCommands::init);
        safeRunnable(Events::init);

        Main.warpRegistry = new CrownWarpRegistry();
        Main.kitRegistry = new CrownKitRegistry();

        Registries.COMVAR_TYPES.close();
    }
}
