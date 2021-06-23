package net.forthecrown.serializer;

import org.bukkit.plugin.Plugin;

/**
 * An abstract class to make file serialization and deserialization easier
 * @param <P> The plugin that this serializer will belong to
 */
public interface CrownSerializer<P extends Plugin> {

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
