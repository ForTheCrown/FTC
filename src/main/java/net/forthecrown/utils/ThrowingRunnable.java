package net.forthecrown.utils;

/**
 * A runnable which throws a throwable of some-kind that must be caught.
 */
public interface ThrowingRunnable<T extends Throwable> {
    void run() throws T;
}