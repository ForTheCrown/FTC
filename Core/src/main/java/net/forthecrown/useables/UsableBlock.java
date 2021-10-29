package net.forthecrown.useables;

import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.Deletable;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.block.TileState;

/**
 * Represents a usable block
 */
public interface UsableBlock extends CrownSerializer, Deletable, Usable {

    /**
     * Gets the location of the block
     * @return The block's location
     */
    WorldVec3i getLocation();

    /**
     * Gets the Tile Entity of the block
     * @return The tile entity
     */
    TileState getBlock();
}
