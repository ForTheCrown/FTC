package net.forthecrown.regions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.utils.math.Vector3i;
import org.bukkit.Location;

import java.util.Objects;

import static net.forthecrown.regions.RegionConstants.REGION_SIZE;

/**
 * Represents the position of a region pole in a 2 dimensional X Z grid.
 */
public class RegionPos {

    private final int x;
    private final int z;

    public RegionPos(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public RegionPos(double x, double z) {
        this((int) x, (int) z);
    }

    public RegionPos add(int x, int z) {
        return new RegionPos(this.x + x, this.z + z);
    }

    public RegionPos subtract(int x, int z) {
        return add(-x, -z);
    }

    /**
     * Gets the relative X cord of this region
     * @return The region's relative X cord
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the relative Z cord of this region
     * @return The region's relative Z cord
     */
    public int getZ() {
        return z;
    }

    /**
     * Gets the absolute world X cord of this region
     * @return The absolute world X cord of this region
     */
    public int getAbsoluteX() {
        return toAbsolute(x);
    }

    /**
     * Gets the absolute world Z cord of this region
     * @return The absolute world Z cord of this region
     */
    public int getAbsoluteZ() {
        return toAbsolute(z);
    }

    /**
     * Creates an 2D vector with the absolute cords of this region
     * @return The absolute 2D vector for this region
     */
    public BlockVector2 toAbsoluteVector() {
        return BlockVector2.at(getAbsoluteX(), getAbsoluteZ());
    }

    /**
     * Turns a relative int cord into absolute
     * <p></p>
     * All this really does is multiply the relative paramater with {@link RegionConstants#REGION_SIZE}
     * @param relative The relative cord
     * @return The absolute value of the relative cord
     */
    public static int toAbsolute(int relative) {
        return relative * REGION_SIZE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionPos cords = (RegionPos) o;
        return getX() == cords.getX() &&
                getZ() == cords.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getZ());
    }

    @Override
    public String toString() {
        return x + " " + z;
    }

    /**
     * Parses region cords from a string
     * <p></p>
     * An example for what this parses is: "-6 4", the characters before
     * the white space being the X cord and the ones after being the Z
     * cord.
     *
     * <p></p>
     * How much of that text was obvious? lol
     *
     * @param str The string to parse
     * @return The position parsed from the string, or null, if parsing failed
     */
    public static RegionPos fromString(String str) {
        try {
            StringReader reader = new StringReader(str);

            int x = reader.readInt();
            reader.skipWhitespace();
            int z = reader.readInt();

            return new RegionPos(x, z);
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("Invalid parse input: " + str);
        }
    }

    /**
     * Gets the region cords from the given location
     * @param loc The location to get region cords from
     * @return Region cords of the given location
     */
    public static RegionPos of(Location loc) {
        return fromAbsolute(loc.getBlockX(), loc.getBlockZ());
    }

    /**
     * Gets the region cords from the given BlockVector2
     * @param vec2 The BlockVector2 to get region cords from
     * @return Region cords of the given BlockVector2
     */
    public static RegionPos of(BlockVector2 vec2) {
        return fromAbsolute(vec2.getX(), vec2.getZ());
    }

    /**
     * Gets the region cords from the given BlockPos
     * @param pos The BlockPos to get region cords from
     * @return Region cords of the given BlockPos
     */
    public static RegionPos of(Vector3i pos) {
        return fromAbsolute(pos.getX(), pos.getZ());
    }

    /**
     * Gets regions cords from 2 absolute x and z cords.
     * @param x The absolute x cord
     * @param z The absolute z cord
     * @return The relative region cords gotten from the 2 absolute cords.
     */
    public static RegionPos fromAbsolute(int x, int z) {
        return new RegionPos(
                fromAbsoluteCord(x),
                fromAbsoluteCord(z)
        );
    }

    /**
     * Gets a relative region cord from an absolute cord
     * @param absolute The absolute cord
     * @return The relative region cord
     */
    public static int fromAbsoluteCord(int absolute) {
        return absolute / REGION_SIZE;
    }
}
