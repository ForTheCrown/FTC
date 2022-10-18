package net.forthecrown.core;

import net.forthecrown.economy.Economy;
import net.forthecrown.vars.VarRegistry;
import net.kyori.adventure.key.Namespaced;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class that looks nice and does all the api stuff
 * <p>
 * Implementation: {@link Main}
 */
public interface Crown extends Plugin, Namespaced {
    String LOGGER_NAME = "FTC";

    /**
     * Gets the FTC plugin instance
     * @return FTC plugin instance
     */
    static Main plugin() {
        return JavaPlugin.getPlugin(Main.class);
    }

    static Logger logger() {
        return LogManager.getLogger(LOGGER_NAME);
    }

    /** Saves everything in the FTC plugin that can be saved */
    static void saveFTC() {
        Main.announcer.save();
        Main.config.save();

        Main.economy.save();
        Main.varRegistry.save();

        logger().info("Saved FTC");
    }

    /**
     * Gets the variable registry instance
     * @return Var registry
     */
    static VarRegistry getVars() {
        return Main.varRegistry;
    }

    static Announcer getAnnouncer() {
        return Main.announcer;
    }

    static Economy getEconomy() {
        return Main.economy;
    }

    static JoinInfo getJoinInfo() {
        return Main.joinInfo;
    }

    static ServerRules getRules() {
        return Main.rules;
    }

    static boolean inDebugMode() {
        return plugin().debugMode;
    }

    static Component prefix() {
        return config().prefix();
    }

    static FtcConfig config() {
        return Main.config;
    }
}