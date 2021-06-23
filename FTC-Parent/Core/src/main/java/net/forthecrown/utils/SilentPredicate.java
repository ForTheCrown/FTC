package net.forthecrown.utils;

/**
 * Like predicate, but silent lol
 * @param <T>
 */
@FunctionalInterface
public interface SilentPredicate<T> {
    boolean testSilent(T value);
}
