package net.forthecrown.regions;

import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.NbtCompoundSavable;
import org.bukkit.World;

import java.util.Collection;
import java.util.Set;

/**
 * An object which manages regions.
 */
public interface RegionManager extends CrownSerializer, NbtCompoundSavable {

    /**
     * Gets the world this manager operates in
     * @return The manager's world
     */
    World getWorld();

    /**
     * Gets a region by it's cords
     * @param cords The cords to get a region by
     * @return The region at the given cords
     */
    PopulationRegion get(RegionPos cords);

    /**
     * Gets a region by it's name
     * @param name The name to get a region by
     * @return The region with the given name, or null, if no region exists by the given name
     */
    PopulationRegion get(String name);

    /**
     * Gets a snapshot of a region
     * @param pos The region position to get the snapshot of
     * @return The region's snapshot, will be {@link PopulationRegion} if
     *         the region is important, {@link RegionAccess.Empty} otherwise
     */
    RegionAccess getSnapshot(RegionPos pos);

    /**
     * Renames a given region to the given name.
     * @param region The region to rename
     * @param newName The new name the region will have
     * @param special Whether the region was paid for on the web-store
     */
    void rename(PopulationRegion region, String newName, boolean special);

    /**
     * Adds a region to the manager
     * @param region The region to add
     */
    void add(PopulationRegion region);

    /**
     * Removes a region from this manager
     * @param region The region to remove
     */
    void remove(PopulationRegion region);

    /**
     * Resets a region
     * <p></p>
     * Warning: resetLand does not currently function
     * @param region The region to reset
     */
    void reset(PopulationRegion region);

    /**
     * Gets all region names
     * @return Region names
     */
    Set<String> getRegionNames();

    /**
     * Gets all named regions
     * @return All named regions
     */
    Collection<PopulationRegion> getNamedRegions();

    /**
     * Removes all 'unimportant' regions from the manager's tracking.
     * <p></p>
     * Note: unimportant refers to the return result of {@link PopulationRegion#isImportant()},
     * If that returns false, the region is considered 'unimportant'
     */
    void dropUnimportantRegions();

    /**
     * Gets the region pole generator attached to this manager
     * @return The manager's pole generator
     */
    RegionPoleGenerator getGenerator();
}