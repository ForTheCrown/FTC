 package net.forthecrown.registry;

import net.kyori.adventure.key.Keyed;

/**
 * Represents a map like object which can store object with a {@link net.kyori.adventure.key.Key} as the, well, key.
 * @param <T>
 */
public interface Registry<T> extends FtcRegistry<T, T>, Iterable<T>, Keyed {
    /**
     * Gets the size of the registry
     * @return the size of the registry
     */
    int size();

    /**
     * Clears the registry
     */
    void clear();

    /**
     * Checks whether the registry is empty
     * @return Whether the registry is empty or not
     */
    boolean isEmpty();

    default <V extends Keyed> V register(V val) {
        return (V) register(val.key(), (T) val);
    }
}
