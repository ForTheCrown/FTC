package net.forthecrown.utils.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Math utility functions that somehow no math library offers.
 */
public final class MathUtil {
    private MathUtil() {}

    public static double clamp(double val, double min, double max) {
        return val <= max ? (val >= min ? val : min) : max;
    }

    public static long clamp(long val, long min, long max) {
        return val <= max ? (val >= min ? val : min) : max;
    }

    public static int clamp(int val, int min, int max) {
        return val <= max ? (val >= min ? val : min) : max;
    }

    public static boolean isInRange(double check, double min, double max) {
        return check >= min && check <= max;
    }

    public static Vector vectorBetweenPoints(Location l1, Location l2) {
        return l1.toVector().clone().subtract(l2.toVector());
    }
}