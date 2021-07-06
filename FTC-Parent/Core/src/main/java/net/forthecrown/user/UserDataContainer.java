package net.forthecrown.user;

import com.google.gson.JsonElement;
import net.kyori.adventure.key.Key;
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
    void set(Key key, JsonElement section);

    /**
     * Gets a plugin's section.
     * <p>Returns an empty section if the plugin has no set section</p>
     * @param key The plugin of which to get the section of
     * @return The plugin's section
     */
    @NotNull JsonElement get(Key key);

    /**
     * returns if the data container is empty
     * @return Whether the container is empty
     */
    boolean isEmpty();

    /**
     * Removes the plugin's section from the data container
     * @param key The plugin's section to remove
     */
    void remove(Key key);

    boolean has(Key key);

    /**
     * Gets the user that this container belongs to
     * @return The owning user
     */
    @NotNull CrownUser getUser();
}
