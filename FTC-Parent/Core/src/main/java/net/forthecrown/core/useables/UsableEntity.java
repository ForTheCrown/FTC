package net.forthecrown.core.useables;

import org.bukkit.entity.Entity;

import java.util.UUID;

public interface UsableEntity extends Usable {
    Entity getEntity();
    UUID getUniqueId();
}
