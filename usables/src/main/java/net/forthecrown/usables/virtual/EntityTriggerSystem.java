package net.forthecrown.usables.virtual;

import static net.forthecrown.usables.virtual.EntityTriggerSystem.LOGGER;
import static net.forthecrown.usables.virtual.EntityTriggerSystem.TRIGGER_KEY;
import static net.forthecrown.usables.virtual.EntityTriggerSystem.newTriggerMap;

import net.forthecrown.Loggers;
import net.forthecrown.events.Events;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.slf4j.Logger;

public class EntityTriggerSystem implements TriggerSystem<EntityTrigger> {

  static final Logger LOGGER = Loggers.getLogger();

  static final NamespacedKey TRIGGER_KEY = new NamespacedKey("usables", "entity_triggers");

  EntityTriggerListener listener;

  static TriggerMap<EntityAction> newTriggerMap() {
    return new TriggerMap<>(EntityAction.CODEC);
  }

  @Override
  public void initializeSystem(VirtualUsableManager manager) {
    listener = new EntityTriggerListener(manager);
    Events.register(listener);
  }

  @Override
  public void onTriggerLoaded(VirtualUsable usable, EntityTrigger trigger) {

  }

  @Override
  public void onTriggerAdd(VirtualUsable usable, EntityTrigger trigger) {

  }

  @Override
  public void onTriggerRemove(VirtualUsable usable, EntityTrigger trigger) {
  }
}

class EntityTriggerListener implements Listener {

  private final VirtualUsableManager manager;

  public EntityTriggerListener(VirtualUsableManager manager) {
    this.manager = manager;
  }

  private TriggerMap<EntityAction> loadMap(Entity entity) {
    var map = newTriggerMap();

    map.loadFromContainer(entity.getPersistentDataContainer(), TRIGGER_KEY)
        .mapError(s -> "Failed to load triggers inside entity " + entity + ": " + s)
        .resultOrPartial(LOGGER::error);

    return map;
  }

  private void saveMap(TriggerMap<EntityAction> map, Entity entity) {
    map.saveToContainer(entity.getPersistentDataContainer(), TRIGGER_KEY)
        .mapError(s -> "Failed to save triggers inside entity " + entity + ": " + s)
        .resultOrPartial(LOGGER::error);
  }

  @EventHandler(ignoreCancelled = true)
  public void onEntityDeath(EntityDeathEvent event) {
    var entity = event.getEntity();
    var killer = entity.getKiller();

    if (killer == null) {
      return;
    }

    var map = loadMap(entity);

    entity.damage(0, null);

    var refs = map.get(EntityAction.ON_ENTITY_INTERACT);

    if (refs.isEmpty()) {
      return;
    }

    Triggers.runReferences(
        refs, manager, killer, event,

        null,

        null
    );
  }
}