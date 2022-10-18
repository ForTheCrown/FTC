package net.forthecrown.utils;

/**
 * A runnable which throws a throwable of somekind that must be caught.
 * Only used by the {@link Util#runSafe(ThrowingRunnable)} method
 */
public interface ThrowingRunnable {
    void run() throws Throwable;
}