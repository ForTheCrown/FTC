package net.forthecrown.core.useables;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.serializer.CrownSerializer;
import net.forthecrown.core.serializer.Deleteable;
import org.bukkit.Location;
import org.bukkit.block.TileState;

public interface UsableBlock extends CrownSerializer<CrownCore>, Deleteable, Usable {
    Location getLocation();
    TileState getSign();
}
