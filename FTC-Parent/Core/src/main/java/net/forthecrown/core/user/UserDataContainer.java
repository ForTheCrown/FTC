package net.forthecrown.core.user;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Represents information that can be stored in a user's data by other plugins
 */
public interface UserDataContainer {

    /**
     * Sets a plugin's section
     * @param key The plugin to set the section of
     * @param section The section
     */
    void set(Plugin key, ConfigurationSection section);

    /**
     * Gets a plugin's section.
     * <p>Returns an empty section if the plugin has no set section</p>
     * @param key The plugin of which to get the section of
     * @return The plugin's section
     */
    @NotNull ConfigurationSection get(Plugin key);

    /**
     * Creates a section for the plugin, re writes any previous section of the same plugin
     * @param key The plugin for which to create a section
     * @return The created section
     */

    @NotNull ConfigurationSection createSection(Plugin key);

    /**
     * returns if the data container is empty
     * @return Whether the container is empty
     */
    boolean isEmpty();

    /**
     * Removes the plugin's section from the data container
     * @param key The plugin's section to remove
     */
    void remove(Plugin key);

    /**
     * Gets the user that this container belongs to
     * @return The owning user
     */
    @NotNull CrownUser getUser();
}
