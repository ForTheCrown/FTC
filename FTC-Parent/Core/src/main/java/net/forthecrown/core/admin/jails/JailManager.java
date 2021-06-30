package net.forthecrown.core.admin.jails;

import net.forthecrown.events.dynamic.JailListener;
import net.forthecrown.registry.Registry;
import net.forthecrown.serializer.CrownSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Stores all the jail locations and names
 */
public interface JailManager extends CrownSerializer, Registry<Location>{
    JailListener getJailListener(Player player);
    void addJailListener(JailListener listener);
    void removeJailListener(JailListener listener);
}
