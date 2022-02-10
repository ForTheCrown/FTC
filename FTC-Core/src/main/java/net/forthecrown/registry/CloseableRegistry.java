package net.forthecrown.registry;

import net.kyori.adventure.key.Key;

/**
 * A registry which can be closed down at anytime and made immutable
 * @param <T> The type for the registry to hold
 */
public interface CloseableRegistry<T> extends Registry<T> {
    /**
     * Closes the registry
     */
    void close();

    /**
     * Checks whether the registry is open
     * @return Whether the registry is open
     */
    boolean isOpen();

    @Override
    T register(Key key, T value);

    @Override
    T remove(Key key);

    @Override
    void clear();
}
