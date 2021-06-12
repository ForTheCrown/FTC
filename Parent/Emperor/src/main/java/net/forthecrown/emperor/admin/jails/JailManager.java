package net.forthecrown.emperor.admin.jails;

import net.forthecrown.emperor.CrownCore;
import net.forthecrown.emperor.events.JailListener;
import net.forthecrown.emperor.registry.Registry;
import net.forthecrown.emperor.serializer.CrownSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Stores all the jail locations and names
 */
public interface JailManager extends CrownSerializer<CrownCore>, Registry<Location>{
    JailListener getJailListener(Player player);
    void addJailListener(JailListener listener);
    void removeJailListener(JailListener listener);
}
