package net.forthecrown.user;

import com.google.gson.JsonElement;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Represents information that can be stored in a user's data
 */
public interface UserDataContainer extends UserAttachment {

    /**
     * Sets a plugin's section
     * @param key The Key to get the section of
     * @param section The section
     */
    void set(Key key, JsonElement section);

    /**
     * Gets a plugin's section.
     * <p>Returns an empty section if the plugin has no set section</p>
     * @param key The key of which to get the section of
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
     * @param key The key's section to remove
     */
    void remove(Key key);

    /**
     * Checks if the container contains the given key
     * @param key The key to check
     * @return Whether the container has the given key
     */
    boolean has(Key key);
}
