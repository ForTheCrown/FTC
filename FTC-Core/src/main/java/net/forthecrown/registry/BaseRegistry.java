package net.forthecrown.registry;

import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.Set;

/**
 * A base of a registry
 * <p></p>
 * Not going to lie, this class exists only so the classes
 * {@link net.forthecrown.useables.warps.WarpManager} and
 * {@link net.forthecrown.useables.kits.KitManager} and
 * {@link Registry} would have a common interface. Also,
 * there was a time when every registry was its own
 * subclass of {@link BaseRegistry}, man was that an awful
 * idea lmao.
 *
 * @param <T> The type the registry contains
 * @param <R> The 'raw' type the registry contains.
 *           Another relic from the Warp and Kit registries,
 *           the raw type is something you'd use in a create
 *           part of the edit command for warps and kits, eg:
 *           the raw type for a warp is a location, since that's
 *           all that's needed to create a warp.
 *
 *           God I'm dumb
 */
public interface BaseRegistry<T, R> {
    /**
     * Gets the item in the registry attached to the given key
     * @param key The value's key
     * @return The key's value, null, if the key has no value
     */
    T get(Key key);

    /**
     * Registers a key-value pair into the registry
     * @param key The key of the pair
     * @param value The value of the pair
     * @return The registered key-value-pair
     */
    T register(Key key, R value);

    /**
     * Removes the key-value pair of the given key
     * @param key The key to remove
     * @return The removed value, null, if nothing was removed
     */
    T remove(Key key);

    /**
     * Gets all keys contained within the map
     * @return All registered keys
     */
    Set<Key> keySet();

    /**
     * Checks if the registry contains the given key
     * @param key The key to check
     * @return True, if the key is registered in this registry, false otherwise
     */
    boolean contains(Key key);

    /**
     * Checks if the registry contains the given value
     * @param value The value to check
     * @return True, if the value is contained in the map, false otherwise
     */
    boolean contains(T value);

    /**
     * Gets all values stored in the registry
     * @return All registered values
     */
    Collection<T> values();
}
