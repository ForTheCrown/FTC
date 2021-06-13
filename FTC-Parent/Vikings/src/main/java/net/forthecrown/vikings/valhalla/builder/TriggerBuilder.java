package net.forthecrown.vikings.valhalla.builder;

import net.forthecrown.vikings.valhalla.data.UnbuiltTrigger;
import net.forthecrown.vikings.valhalla.triggers.TriggerableEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;

public class TriggerBuilder {

    private final List<UnbuiltTrigger<?>> triggers;

    private final List<TriggerableEvent<EntityDeathEvent>> deathTriggers = new ArrayList<>();
    private final List<TriggerableEvent<EntityDamageByEntityEvent>> damageTriggers = new ArrayList<>();
    private final List<TriggerableEvent<PlayerMoveEvent>> moveTriggers = new ArrayList<>();
    private final List<TriggerableEvent<PlayerInteractEntityEvent>> interactionTriggers = new ArrayList<>();
    private final List<TriggerableEvent<PlayerInteractEvent>> blockInteractionTriggers = new ArrayList<>();

    public TriggerBuilder(List<UnbuiltTrigger<?>> triggers) {
        this.triggers = triggers;
        sortIntoLists();
    }

    private void sortIntoLists(){
    }
}
