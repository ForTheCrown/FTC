package net.forthecrown.core;

import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public @UtilityClass class FTC {
    /**
     * Gets the FTC plugin instance
     * @return FTC plugin instance
     */
    public Main getPlugin() {
        return JavaPlugin.getPlugin(Main.class);
    }

    public Logger getLogger() {
        return getPlugin().logger;
    }

    public boolean inDebugMode() {
        return getPlugin().debugMode;
    }
}