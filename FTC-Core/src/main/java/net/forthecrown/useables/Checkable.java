package net.forthecrown.useables;

import net.forthecrown.useables.checks.UsageCheckInstance;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;

import java.util.List;
import java.util.Set;

/**
 * Represents an object which has UsageChecks.
 */
public interface Checkable extends UsableObject {

    /**
     * Lists all the usage checks this object has
     * <p>
     * This most likey needs to be changed as I often store
     * checks in a map to prevent several of one type existing
     * </p>
     * @return The object's usage checks
     */
    List<UsageCheckInstance> getChecks();

    /**
     * Adds a check
     * @param precondition The check to add
     */
    void addCheck(UsageCheckInstance precondition);

    /**
     * Removes a check with the given key
     * @param name The key to remove
     */
    void removeCheck(Key name);

    /**
     * Clears all checks
     */
    void clearChecks();

    /**
     * Gets the check keys
     * @return The check keys
     */
    Set<Key> getCheckTypes();

    /**
     * Gets the check keys as strings
     * @return The check keys
     */
    default Set<String> getStringCheckTypes(){
        return ListUtils.convertToSet(getCheckTypes(), Key::asString);
    }

    /**
     * Gets a check with the given key and casts it to the given class
     * @param key The check to get
     * @param clazz The class to get it as
     * @param <T> The type
     * @return The check as the given type
     * @throws ClassCastException If the type the key returns doesn't match with the class given
     */
    <T extends UsageCheckInstance> T getCheck(Key key, Class<T> clazz) throws ClassCastException;
}
