package net.forthecrown.useables;

import net.forthecrown.useables.actions.UsageActionInstance;
import net.kyori.adventure.key.Key;

import java.util.List;

/**
 * Represents an object which holds actions
 */
public interface Actionable {

    /**
     * Adds an action to this objects action list
     * @param action The action to add
     */
    void addAction(UsageActionInstance action);

    /**
     * Removes a usage by the given index
     * @param index The index to remove
     */
    void removeAction(int index);

    /**
     * Lists all the actions of this object
     * @return The object's actions
     */
    List<UsageActionInstance> getActions();

    /**
     * Clears all the actions
     */
    void clearActions();

    /**
     * Gets an action by the given key and returns it as the given type
     * @param key The key to get by
     * @param clazz The class of the type
     * @param <T> The type to return as
     * @return The action with the given key and as the given type
     * @throws ClassCastException If the type under the given key is not the given type
     */
    <T extends UsageActionInstance> T getAction(Key key, Class<T> clazz) throws ClassCastException;
}
