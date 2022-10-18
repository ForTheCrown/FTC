package net.forthecrown.utils.math;

/**
 * Math utility functions that somehow no math library offers.
 */
public final class MathUtil {
    private MathUtil() {}

    public static boolean inRange(long val, long min, long max) {
        if (val < min) return false;
        if (val > max) return false;
        return true;
    }
}