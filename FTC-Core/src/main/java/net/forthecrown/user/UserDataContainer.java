package net.forthecrown.user;

import com.google.gson.JsonElement;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.Nullable;

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

    default void set(UserDataAccessor a, JsonElement element) {
        set(a.accessKey(), element);
    }

    /**
     * Gets a plugin's section.
     * <p>Returns an empty section if the plugin has no set section</p>
     * @param key The key of which to get the section of
     * @return The plugin's section
     */
    @Nullable JsonElement get(Key key);

    default @Nullable JsonElement get(UserDataAccessor a) {
        return get(a.accessKey());
    }

    default JsonElement getOrDefault(Key key, JsonElement def) {
        JsonElement result = get(key);
        return result == null ? def : result;
    }

    default JsonElement getOrDefault(UserDataAccessor a, JsonElement def) {
        return getOrDefault(a.accessKey(), def);
    }

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

    default void remove(UserDataAccessor a) {
        remove(a.accessKey());
    }

    /**
     * Checks if the container contains the given key
     * @param key The key to check
     * @return Whether the container has the given key
     */
    boolean has(Key key);

    default boolean has(UserDataAccessor a) {
        return has(a.accessKey());
    }

    int size();

    void clear();
}
