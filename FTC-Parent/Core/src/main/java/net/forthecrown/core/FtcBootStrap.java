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
import net.forthecrown.useables.kits.CrownKitManager;
import net.forthecrown.useables.warps.CrownWarpManager;
import net.forthecrown.registry.Registries;
import net.forthecrown.serializer.UserJsonSerializer;
import net.forthecrown.useables.CrownUsablesManager;
import net.forthecrown.useables.actions.UsageActions;
import net.forthecrown.useables.preconditions.UsageChecks;
import net.forthecrown.user.FtcUserManager;
import net.forthecrown.valhalla.ValhallaEngine;

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

        Main.shopManager = new CrownShopManager();
        Main.userManager = new FtcUserManager();
        Main.punishmentManager = new CrownPunishmentManager();
        Main.usablesManager = new CrownUsablesManager();
        Main.jailManager = new CrownJailManager();

        Main.kingship = new CrownKingship();
        Main.rules = new ServerRules();

        if(ForTheCrown.inDebugMode()) safeRunnable(ValhallaEngine::init);
        safeRunnable(Pirates::init);
        safeRunnable(Bosses::init);
        safeRunnable(Cosmetics::init);
        safeRunnable(UsageChecks::init);
        safeRunnable(UsageActions::init);
        safeRunnable(CoreCommands::init);
        safeRunnable(Events::init);

        Main.warpRegistry = new CrownWarpManager();
        Main.kitRegistry = new CrownKitManager();

        Registries.COMVAR_TYPES.close();
    }
}
