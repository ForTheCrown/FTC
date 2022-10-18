package net.forthecrown.utils.io;

import java.io.IOException;

/**
 * A type of {@link java.util.function.Consumer}
 * which is allowed to throw {@link IOException}
 * instances.
 *
 * @param <T> The type to consume
 */
@FunctionalInterface
public interface IOConsumer<T> {
    void accept(T path) throws IOException;
}