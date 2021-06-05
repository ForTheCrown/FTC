package net.forthecrown.emperor.admin.jails;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.registry.Registry;
import net.forthecrown.emperor.serializer.CrownSerializer;
import org.bukkit.Location;

/**
 * Stores all the jail locations and names
 */
public interface JailManager extends CrownSerializer<CrownCore>, Registry<Location>{
}
