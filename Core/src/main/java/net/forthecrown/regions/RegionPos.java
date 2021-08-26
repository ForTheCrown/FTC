package net.forthecrown.regions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.BlockVector2;
import net.forthecrown.utils.math.Vector3i;
import org.bukkit.Location;

import java.util.Objects;

import static net.forthecrown.regions.RegionConstants.HALF_REGION_SIZE;
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

    /**
     * Creates a region pos with cords of this region added to the given x and z
     * @param x The X cord to add
     * @param z The Z cord to add
     * @return A new region pos with the x and z of this pos added to the given x and z
     */
    public RegionPos add(int x, int z) {
        return new RegionPos(this.x + x, this.z + z);
    }

    /**
     * Same as {@link RegionPos#add(int, int)} except it subtracts.
     * @param x The X cord to subtract
     * @param z The Z cord to subtract
     * @return Same as {@link RegionPos#add(int, int)}, except subtracted
     */
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
    public int getCenterX() {
        return toCenter(x);
    }

    /**
     * Gets the absolute world Z cord of this region
     * @return The absolute world Z cord of this region
     */
    public int getCenterZ() {
        return toCenter(z);
    }

    /**
     * Gets the absolute x of this pos
     * @return Absolute world x
     */
    public int getAbsoluteX() {
        return toAbsolute(x);
    }

    /**
     * Gets the absolute z cord of this pos
     * @return Absolute world z
     */
    public int getAbsoluteZ() {
        return toAbsolute(z);
    }

    /**
     * Creates an 2D vector with the region-centered absolute cords of this region
     * @return The region-centered 2D vector for this region
     */
    public BlockVector2 toCenter() {
        return BlockVector2.at(getCenterX(), getCenterZ());
    }

    /**
     * Creates a 2D vector with the absolute cords of this region
     * @return The region's absolute vector
     */
    public BlockVector2 toAbsolute() {
        return BlockVector2.at(getAbsoluteX(), getAbsoluteZ());
    }

    /**
     * Turns a relative int cord into absolute cord centered in a region
     * @param relative The relative coordinate
     * @return The region-centered coordinated
     */
    public static int toCenter(int relative) {
        return toAbsolute(relative) + HALF_REGION_SIZE;
    }

    /**
     * Turns a relative int cord into an absolute world coordinate
     * @param relative The relative coordinate
     * @return The absolute coordinate
     */
    public static int toAbsolute(int relative) {
        return relative * REGION_SIZE;
    }

    /**
     * Gets a relative region cord from an absolute cord
     * @param absolute The absolute cord
     * @return The relative region cord
     */
    public static int fromAbsolute(int absolute) {
        int relative = absolute / REGION_SIZE;
        if(absolute < 0) relative--;

        return relative;
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
                fromAbsolute(x),
                fromAbsolute(z)
        );
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
}
