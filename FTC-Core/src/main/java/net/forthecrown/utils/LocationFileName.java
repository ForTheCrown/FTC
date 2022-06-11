package net.forthecrown.utils;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * A quick thing that {@link net.forthecrown.economy.shops.SignShop} used to
 * use for its filenames. Currently only used by the {@link net.forthecrown.useables.UsableBlock}.
 * <p>
 * This class denotes a location saved as an underscore-separated string.
 * Example: 'world_void_156_41_1265'
 */
public record LocationFileName(String world,
                               int x, int y, int z)
{

    /**
     * Parses a location file name from the given string
     * @param fName The string to parse
     * @return The parsed result
     * @throws IllegalStateException If the parsing failed
     */
    public static LocationFileName parse(String fName) {
        try {
            // lastIndex meaning the index before the file type, so we don't read the .json at the end
            // or something like that
            int lastIndex = fName.lastIndexOf('.');
            if (lastIndex == -1) lastIndex = fName.length();

            StringReader reader = new StringReader(fName.substring(0, lastIndex));

            //Read the world name at the start
            while (reader.canRead() && !StringReader.isAllowedNumber(reader.peek())) {
                reader.skip();
            }

            // Get the world name, - 1 from the length since it ends with the "_" before the integer
            String read = reader.getRead();
            String world = read.substring(0, read.length() - 1);

            //Read cords
            int x = reader.readInt();
            reader.skip();
            int y = reader.readInt();
            reader.skip();
            int z = reader.readInt();

            return new LocationFileName(world, x, y, z);
        } catch (CommandSyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static LocationFileName of(Location l) {
        return new LocationFileName(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    public static LocationFileName of(WorldVec3i v) {
        return new LocationFileName(v.getWorld().getName(), v.getX(), v.getY(), v.getZ());
    }

    public Location toLocation() {
        return new Location(getWorld(), x, y, z);
    }

    public WorldVec3i toVector() {
        return new WorldVec3i(getWorld(), x, y, z);
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    @Override
    public String toString() {
        return world + '_' + x + '_' + y + '_' + z;
    }

    public String toString(String suffix) {
        return this + suffix;
    }
}