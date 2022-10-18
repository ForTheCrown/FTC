package net.forthecrown.core;

import net.forthecrown.commands.manager.Commands;
import net.forthecrown.core.admin.BannedWords;
import net.forthecrown.core.admin.Punishments;
import net.forthecrown.core.config.Configs;
import net.forthecrown.core.holidays.ServerHolidays;
import net.forthecrown.core.resource.ResourceWorldTracker;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.datafix.Transformers;
import net.forthecrown.dungeons.Bosses;
import net.forthecrown.dungeons.Dungeons;
import net.forthecrown.dungeons.boss.evoker.EvokerVars;
import net.forthecrown.dungeons.enchantments.FtcEnchants;
import net.forthecrown.economy.Economy;
import net.forthecrown.events.Events;
import net.forthecrown.grenadier.exceptions.RoyalCommandException;
import net.forthecrown.inventory.ExtendedItems;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.structure.Structures;
import net.forthecrown.text.ChatEmotes;
import net.forthecrown.useables.Usables;
import net.forthecrown.user.Components;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.property.Properties;
import net.forthecrown.vars.VarRegistry;
import net.forthecrown.vars.types.VarTypes;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static net.forthecrown.core.Crown.plugin;
import static net.forthecrown.core.Main.*;

/**
 * A class which loads and creates everything the FTC plugin does and needs.
 * Aka a class which starts the plugin
 */
final class BootStrap {
    private BootStrap() {}

    static final Logger LOGGER = LogManager.getLogger("FTC Bootstrap");
    static final String INIT_METHOD = "init";

    static boolean loaded;

    static void initConfig() {
        config = new FtcConfig();
        config.read();
    }

    static void initVars() {
        VarTypes.init();
        varRegistry = new VarRegistry();
        varRegistry.registerVars(Vars.class);
    }

    static void load() {
        RoyalCommandException.ENABLE_HOVER_STACK_TRACE = Crown.inDebugMode();

        announcer   = new Announcer();

        init(ChatEmotes.class);
        init(FtcFlags.class);
        init(Transformers.class);

        loaded = true;
    }

    static void enable() {
        // Can happen during reloads, onLoad
        // Does not get called twice
        if (!loaded) {
            plugin().onLoad();
        }

        // Initialize config sections
        joinInfo        = new JoinInfo();
        rules           = new ServerRules();

        config.addSection(rules);
        config.addSection(joinInfo);

        // Only load here, cuz we've already read the JSON
        config.load();

        economy = new Economy();

        init(UserManager.class);
        init(Configs.class);
        init(ServerHolidays.class);
        init(Structures.class);
        init(RegionManager.class);
        init(FtcEnchants.class);
        init(EvokerVars.class);
        init(Bosses.class);
        init(ExtendedItems.class);
        init(Cosmetics.class);
        init(Components.class);
        init(Properties.class);
        init(Usables.class);
        init(ResourceWorldTracker.class);
        init(Commands.class);
        init(Events.class);
        init(Dungeons.class);

        plugin().saveResource("banned_words.json", true);
        BannedWords.load();

        announcer.start();
        DayChange.get().schedule();
        config.load();

        ServerIcons.loadIcons();
        Transformers.runCurrent();
        Punishments.get().reload();

        economy.reload();
    }

    static void init(Class c) {
        try {
            Method init = c.getDeclaredMethod(INIT_METHOD);
            init.setAccessible(true);

            Validate.isTrue(Modifier.isStatic(init.getModifiers()), "% method is not static", INIT_METHOD);
            Validate.isTrue(init.getReturnType() == Void.TYPE, "%s method return value is not void", INIT_METHOD);

            init.invoke(null);
            LOGGER.info("{} Initialized", c.getSimpleName());
        } catch (Throwable t) {
            LOGGER.error("Couldn't initialize {}:", c.getSimpleName(), t);
        }
    }
}