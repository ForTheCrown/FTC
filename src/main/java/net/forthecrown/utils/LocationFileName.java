package net.forthecrown.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * A simple class that stores a world's name and block coordinates which
 * can be turned into a string. Mainly used for file names.
 * <p>
 * This class denotes a location saved as an underscore-separated string.
 * Example: 'world_void_156_41_1265'
 */
public record LocationFileName(String world,
                               int x, int y, int z
) {

    /** Delimiter character used to separate coordinates from each other and from world name */
    public static final char DELIMITER = '_';

    /**
     * Parses a location file name from the given string
     * @param fName The string to parse
     * @return The parsed result
     * @throws IllegalStateException If the parsing failed
     */
    public static LocationFileName parse(String fName) {
        try {
            if (fName.contains(".")) {
                fName = fName.substring(0, fName.lastIndexOf('.'));
            }

            return parse(new StringReader(fName));
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static LocationFileName parse(StringReader reader) throws CommandSyntaxException {
        //Read the world name at the start
        while (reader.canRead() && !StringReader.isAllowedNumber(reader.peek())) {
            reader.skip();
        }

        // Get the world name, - 1 from the length since it ends with the "_" before the integer
        String read = reader.getRead();
        String world = read.substring(0, read.length() - 1);

        //Read cords
        int x = reader.readInt();
        reader.expect(DELIMITER);
        int y = reader.readInt();
        reader.expect(DELIMITER);
        int z = reader.readInt();

        return new LocationFileName(world, x, y, z);
    }

    public static LocationFileName of(WorldVec3i v) {
        return new LocationFileName(v.getWorld().getName(), v.x(), v.y(), v.z());
    }

    public WorldVec3i toVector() {
        return new WorldVec3i(getWorld(), x, y, z);
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public Block getBlock() {
        return getWorld().getBlockAt(x, y, z);
    }

    @Override
    public String toString() {
        return world + DELIMITER + x + DELIMITER + y + DELIMITER + z;
    }

    public String toString(String suffix) {
        return this + suffix;
    }
}