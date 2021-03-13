package net.forthecrown.core.api;

import org.bukkit.plugin.java.JavaPlugin;

public interface CrownFileManager<P extends JavaPlugin> {

    /**
     * Saves the file
     */
    void save();

    /**
     * Reloads the file
     */
    void reload();

    /**
     * Gets the plugin that created this file
     * @return File owning plugin
     */
    P getPlugin();
}
