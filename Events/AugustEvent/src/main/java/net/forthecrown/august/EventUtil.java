package net.forthecrown.august;

import org.bukkit.entity.Entity;
import org.bukkit.entity.GlowSquid;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class EventUtil {

    public static Entity findPinata() {
        Collection<Entity> entities = EventConstants.PINATA_REGION.getEntitiesByType(GlowSquid.class);

        for (Entity e: entities) {
            if(!e.getPersistentDataContainer().has(EventConstants.PINATA_KEY, PersistentDataType.BYTE)) continue;
            return e;
        }

        A_Main.logger.severe("Could not find pinata entity in pinata region D:");
        return null;
    }

}
