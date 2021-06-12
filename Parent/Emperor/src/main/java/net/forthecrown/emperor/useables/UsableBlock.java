package net.forthecrown.emperor.useables;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.serializer.CrownSerializer;
import net.forthecrown.emperor.serializer.Deleteable;
import org.bukkit.Location;
import org.bukkit.block.TileState;

public interface UsableBlock extends CrownSerializer<CrownCore>, Deleteable, Usable {
    Location getLocation();
    TileState getSign();
}
