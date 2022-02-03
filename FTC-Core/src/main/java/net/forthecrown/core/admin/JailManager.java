package net.forthecrown.core.admin;

import net.forthecrown.events.dynamic.JailListener;
import net.forthecrown.registry.Registry;
import net.forthecrown.serializer.CrownSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Stores all the jail locations and names
 */
public interface JailManager extends CrownSerializer, Registry<Location> {
    JailListener getListener(Player player);
    void addListener(JailListener listener);
    void removeListener(JailListener listener);
}
