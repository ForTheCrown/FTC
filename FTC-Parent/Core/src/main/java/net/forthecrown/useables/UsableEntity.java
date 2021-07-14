package net.forthecrown.useables;

import org.bukkit.entity.Entity;

import java.util.UUID;

/**
 * Represents an entity that can be interacted with
 */
public interface UsableEntity extends Usable {

    /**
     * Gets the entity this object is tied to
     * @return The entity
     */
    Entity getEntity();

    /**
     * Gets the UUID of this entity
     * @return The UUID of the entity.
     */
    UUID getUniqueId();
}
