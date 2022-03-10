package net.forthecrown.dungeons.level;

import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.forthecrown.utils.Nameable;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.key.Keyed;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * A single level in the dungeons
 */
public interface DungeonLevel extends Keyed, Nameable {
    /**
     * Saves the level into the given tag
     * @param tag The tag to save into
     */
    void save(CompoundTag tag);

    /**
     * Loads the level from the given tag
     * @param tag The tag to load from
     */
    void load(CompoundTag tag);

    /**
     * Places all spawners the level has
     */
    void placeSpawners();

    /**
     * Gets a list of 'packed' spawner positions that haven't been destroyed
     *
     * The long is a {@link BlockPos#asLong()} result,
     * they can be made back into BlockPos'es or Vector3i's
     * with {@link net.forthecrown.utils.math.Vector3i#of(long)} or
     * {@link BlockPos#of(long)}
     *
     * @return Packed spawner coordinates
     */
    LongList getExistingSpawners();

    void addSpawner(Vector3i pos, CompoundTag spawnerData);

    default void addSpawner(Vector3i pos, BaseSpawner spawner) {
        addSpawner(pos, spawner.save(new CompoundTag()));
    }

    void addSpawner(Block block);

    default void addSpawner(SpawnerBlockEntity entity) {
        addSpawner(Vector3i.of(entity.getBlockPos()), entity.getSpawner());
    }

    /**
     * Checks whether {@link DungeonLevel#getExistingSpawners()} is empty
     * @return True, if all the spawners in the level have been destroyed,
     *         false otherwise
     */
    boolean isClear();

    /**
     * Makes the player 'view' all the spawners in the level.
     * Displays all spawners as glowing invisible slimes that
     * only the viewer sees.
     *
     * @param player The player viewing the level
     * @return The spawner view, null, if creation failed
     */
    SpawnerView view(Player player);

    /**
     * Closes the given view
     * @param view The view to close
     */
    void stopViewing(SpawnerView view);

    /**
     * Gets the bounds of the level
     * @return The level's bounds
     */
    Bounds3i getBounds();

    void setBounds(Bounds3i bounds);

    /**
     * Gets all spawners in the level
     * @return Level spawner positions
     */
    LongSet getSpawnerPositions();

    BaseSpawner getSpawner(long pos);

    default BaseSpawner getSpawner(Vector3i pos) {
        return getSpawner(pos.toLong());
    }

    void setName(String name);

    /**
     * Gets the world the level is in
     * @return The level's world
     */
    World getWorld();

    void setWorld(World world);
}
