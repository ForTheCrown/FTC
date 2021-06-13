package net.forthecrown.core.admin.jails;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.events.JailListener;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.serializer.CrownSerializer;
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
