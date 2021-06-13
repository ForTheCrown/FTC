package net.forthecrown.vikings.valhalla.triggers;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public enum TriggerType {
    MOVE (PlayerMoveEvent.class),
    INTERACT_BLOCK (PlayerInteractEvent.class),
    INTERACT_MOB (PlayerInteractEntityEvent.class),
    ENTITY_DIE (EntityDeathEvent.class),
    ENTITY_TAKE_DAMAGE (EntityDamageByEntityEvent.class);

    public final Class<? extends Event> eventClass;
    TriggerType(Class<? extends Event> aClass) {
        this.eventClass = aClass;
    }
}
