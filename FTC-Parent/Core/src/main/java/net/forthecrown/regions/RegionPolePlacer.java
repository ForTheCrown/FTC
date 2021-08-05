package net.forthecrown.regions;

import net.forthecrown.utils.math.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Dear god, I don't know enough about structure editing in MC to make this
 */
public class RegionPolePlacer {
    public static boolean needsPole(RegionCords cords, World world) {
        return true;
    }

    public static void placeAt(RegionCords cords, World world) {
        int y = world.getHighestBlockYAt(cords.getAbsoluteX(), cords.getAbsoluteZ());

        BlockPos bottom = new BlockPos(cords.getAbsoluteX(), y, cords.getAbsoluteZ());
        StructurePlaceSettings settings = new StructurePlaceSettings();
    }

    public static void placeAt(Location loc) {
        placeAt(RegionCords.of(loc), loc.getWorld());
    }
}
