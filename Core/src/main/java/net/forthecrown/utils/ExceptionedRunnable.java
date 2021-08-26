package net.forthecrown.utils;

/**
 * A runnable which throws a throwable of somekind that must be caught.
 * Only used by the {@link FtcUtils#safeRunnable(ExceptionedRunnable)} method
 */
public interface ExceptionedRunnable {
    void run() throws Throwable;
}
