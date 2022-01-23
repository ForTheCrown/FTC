package net.forthecrown.crownevents.engine;

import net.forthecrown.utils.math.AbstractVector3i;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public record LocOffset(double x, double y, double z, float yaw, float pitch) {

    public static LocOffset of(Vector vector) {
        return new LocOffset(vector.getX(), vector.getY(), vector.getZ(), 0, 0);
    }

    public static LocOffset of(Location l) {
        return new LocOffset(l.getX(), l.getY(), l.getZ(), l.getYaw(), l.getPitch());
    }

    public static LocOffset of(Location min, double x, double y, double z) {
        return new LocOffset(x - min.getX(), y - min.getY(), z - min.getZ(), min.getYaw(), min.getPitch());
    }

    public static LocOffset of(Location min, Location max) {
        Location offset = max.clone().subtract(min);
        return of(offset);
    }

    public int getBlockX() {
        return toInt(x());
    }

    public int getBlockY() {
        return toInt(y());
    }

    public int getBlockZ() {
        return toInt(z());
    }

    private int toInt(double val) {
        return (int) Math.round(val);
    }

    public Location apply(Location min) {
        Location result = min.clone().add(x, y, z);
        if(yaw != 0) result.setYaw(yaw);
        if(pitch != 0) result.setPitch(pitch);

        return result;
    }

    public Location apply(WorldVec3i min) {
        return apply(min.toLocation());
    }

    public <T extends AbstractVector3i<T>> T applyBlock(T min) {
        return min.clone().add(getBlockX(), getBlockY(), getBlockZ());
    }
}
