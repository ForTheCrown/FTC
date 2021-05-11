package net.forthecrown.core.api;

import org.bukkit.entity.Entity;

import java.util.UUID;

public interface InteractableEntity extends Interactable{
    Entity getEntity();
    UUID getUniqueId();
}
