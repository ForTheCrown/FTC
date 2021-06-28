package net.forthecrown.useables;

import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.serializer.Deletable;
import org.bukkit.Location;
import org.bukkit.block.TileState;

public interface UsableBlock extends CrownSerializer, Deletable, Usable {
    Location getLocation();
    TileState getSign();
}
