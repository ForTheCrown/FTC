package net.forthecrown.regions;

import net.forthecrown.core.Keys;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;

/**
 * A generator of region poles.
 * A special class... that's slower than population density's (._. )
 */
public interface RegionPoleGenerator {
    NamespacedKey TOP_SIGN_KEY = Keys.key("ftc", "region_pole_top");

    /**
     * Generates the pole for the given region
     * @param region The region to generate a pole for
     */
    void generate(PopulationRegion region);

    /**
     * Generates the region name sign ontop of the pole
     * @param pos The sign's position
     * @param region The region
     */
    void generateRegionName(WorldVec3i pos, PopulationRegion region);

    /**
     * Generates a neighbor region name
     * @param pos The position to place the sign at
     * @param signFace On what block face to put the sign on
     * @param direction The direction to get the neighboring region from
     */
    void generateNextRegion(WorldVec3i pos, BlockFace signFace, BlockFace direction, RegionPos origin);

    void generateResidency(WorldVec3i pos, BlockFace direction, RegionResidency residency);

    /**
     * Generates the help side text
     * @param pos The position to place the signs at
     * @param direction The block face to place the sign on
     */
    void generateSideText(WorldVec3i pos, BlockFace direction);

    /**
     * Gets the region manager that owns this generator
     * @return The Region Manager
     */
    RegionManager getManager();
}