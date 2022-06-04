package net.forthecrown.utils.math;

import com.sk89q.worldedit.math.Vector3;
import org.bukkit.Location;
import org.bukkit.util.Vector;

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

    public static boolean inRange(double val, double min, double max) {
        if (val < min) return false;
        if (val > max) return false;
        return true;
    }

    public static Vector3 toWorldEdit(Location l) {
        return toWorldEdit(l.toVector());
    }

    public static Vector3 toWorldEdit(Vector l) {
        return Vector3.at(l.getX(), l.getY(), l.getZ());
    }
}