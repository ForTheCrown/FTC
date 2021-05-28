package net.forthecrown.emperor.utils;

/**
 * Like predicate, but silent lol
 * @param <T>
 */
public interface SilentPredicate<T> {
    boolean testSilent(T value);
}
