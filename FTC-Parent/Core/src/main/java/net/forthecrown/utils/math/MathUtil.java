package net.forthecrown.utils.math;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class MathUtil {
    private MathUtil() {}

    public static double clamp(double val, double min, double max) {
        return val <= max ? (val >= min ? val : min) : max;
    }

    public static long clamp(long val, long min, long max) {
        return val <= max ? (val >= min ? val : min) : max;
    }

    public static boolean isInRange(int check, int min, int max) {
        return check >= min && check <= max;
    }

    public static Vector vectorBetweenPoints(Location l1, Location l2) {
        return l1.toVector().clone().subtract(l2.toVector());
    }
}