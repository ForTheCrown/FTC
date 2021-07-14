package net.forthecrown.registry;

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
}
