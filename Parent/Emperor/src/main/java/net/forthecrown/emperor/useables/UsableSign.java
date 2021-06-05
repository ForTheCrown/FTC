package net.forthecrown.emperor.useables;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.serializer.Deleteable;
import net.forthecrown.emperor.serializer.CrownSerializer;
import org.bukkit.Location;
import org.bukkit.block.Sign;

public interface UsableSign extends CrownSerializer<CrownCore>, Deleteable, Usable {
    Location getLocation();
    Sign getSign();
}
