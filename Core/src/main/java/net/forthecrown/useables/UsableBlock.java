package net.forthecrown.useables;

import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.Deletable;
import org.bukkit.Location;
import org.bukkit.block.TileState;

/**
 * Represents a usable block
 */
public interface UsableBlock extends CrownSerializer, Deletable, Usable {

    /**
     * Gets the location of the block
     * @return The block's location
     */
    Location getLocation();

    /**
     * Gets the Tile Entity of the block
     * @return The tile entity
     */
    TileState getBlock();
}
