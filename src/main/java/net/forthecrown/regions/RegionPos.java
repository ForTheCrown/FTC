package net.forthecrown.regions;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.math.BlockVector2;
import lombok.Getter;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.math.vector.Vector2i;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;

import static net.forthecrown.regions.Regions.HALF_REGION_SIZE;
import static net.forthecrown.regions.Regions.REGION_SIZE;

/**
 * Represents the position of a region in a 2 dimensional X Z grid.
 */
public class RegionPos {
    public static final PersistentDataType<int[], RegionPos> DATA_TYPE = new RegionPosDataType();

    @Getter
    private final int x, z;

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
    public Vector2i toCenter() {
        return Vector2i.from(getCenterX(), getCenterZ());
    }

    /**
     * Creates a 2D vector with the absolute cords of this region
     * @return The region's absolute vector
     */
    public BlockVector2 toAbsolute() {
        return BlockVector2.at(getAbsoluteX(), getAbsoluteZ());
    }

    /**
     * Saves this region position into an integer array
     * @return The saved integer array
     */
    public IntArrayTag save() {
        return new IntArrayTag(new int[] { x, z });
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
    public static int toRegion(int absolute) {
        int relative = absolute / REGION_SIZE;
        if(absolute < 0) relative--;

        return relative;
    }

    /**
     * Same as {@link RegionPos#parse(StringReader)} except it doesn't throw an exception.
     * @param str The string to parse
     * @throws IllegalStateException If the parsing failed
     * @return The pos parsed from the string
     */
    public static RegionPos fromString(String str) throws IllegalStateException {
        try {
            return parse(new StringReader(str));
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException("Invalid parse input: " + str);
        }
    }

    /**
     * Parses region cords from a string
     * <p>
     * An example for what this parses is: "-6 4", the characters before
     * the white space being the X cord and the ones after being the Z
     * cord.
     *
     * @param reader The reader to parse from.
     * @throws CommandSyntaxException if the parsing failed
     * @return The position parsed from the string
     */
    public static RegionPos parse(StringReader reader) throws CommandSyntaxException {
        int x = reader.readInt();
        reader.skipWhitespace();
        int z = reader.readInt();

        return new RegionPos(x, z);
    }

    /**
     * Reads a region position from the given tag.
     * <p>
     * The given tag must be an IntArrayTag which
     * holds the 2 x and z values needed.
     *
     * @param tag The tag to read
     * @return The read position
     */
    public static RegionPos read(Tag tag) {
        int[] arr = ((IntArrayTag) tag).getAsIntArray();
        return new RegionPos(arr[0], arr[1]);
    }

    /**
     * Gets the region cords from the given location
     * @param loc The location to get region cords from
     * @return Region cords of the given location
     */
    public static RegionPos of(Location loc) {
        return of(loc.getBlockX(), loc.getBlockZ());
    }

    /**
     * Gets the region cords from the given BlockVector2
     * @param vec2 The BlockVector2 to get region cords from
     * @return Region cords of the given BlockVector2
     */
    public static RegionPos of(Vector2i vec2) {
        return of(vec2.x(), vec2.y());
    }

    /**
     * Gets the region cords from the given BlockPos
     * @param pos The BlockPos to get region cords from
     * @return Region cords of the given BlockPos
     */
    public static RegionPos of(Vector3i pos) {
        return of(pos.x(), pos.z());
    }

    /**
     * Gets regions cords from 2 absolute x and z cords.
     * @param x The absolute x cord
     * @param z The absolute z cord
     * @return The relative region cords gotten from the 2 absolute cords.
     */
    public static RegionPos of(int x, int z) {
        return new RegionPos(
                toRegion(x),
                toRegion(z)
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

    private static class RegionPosDataType implements PersistentDataType<int[], RegionPos> {
        @Override
        public @NotNull Class<int[]> getPrimitiveType() {
            return int[].class;
        }

        @Override
        public @NotNull Class<RegionPos> getComplexType() {
            return RegionPos.class;
        }

        @Override
        public int @NotNull [] toPrimitive(@NotNull RegionPos complex, @NotNull PersistentDataAdapterContext context) {
            return new int[] { complex.getX(), complex.getZ() };
        }

        @Override
        public @NotNull RegionPos fromPrimitive(int @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
             return new RegionPos(primitive[0], primitive[1]);
        }
    }
}