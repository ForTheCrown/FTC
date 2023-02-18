package net.forthecrown.useables;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.events.Events;
import net.forthecrown.events.TriggerListener;
import net.forthecrown.utils.WorldChunkMap;
import net.forthecrown.utils.math.Bounds3i;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * A class which manages {@link UsableTrigger} instances.
 * <p>
 * The biggest use of this class is the {@link GlobalTriggerManager} used to manage all triggers
 * created by commands and such.
 */
public class TriggerManager {

  public static final double PLAYER_HALF_WIDTH = 0.6D / 2;
  public static final double PLAYER_HEIGHT = 1.8D;

  private final Map<String, UsableTrigger>
      triggers = new Object2ObjectOpenHashMap<>();

  private final WorldChunkMap<UsableTrigger> worldMap = new WorldChunkMap<>();

  @Getter
  private TriggerListener listener;

  public boolean isListenerRegistered() {
    return listener != null;
  }

  public void registerListener() {
    if (isListenerRegistered()) {
      return;
    }

    listener = new TriggerListener(this);
    Events.register(listener);
  }

  public void unregisterListener() {
    if (!isListenerRegistered()) {
      return;
    }

    Events.unregister(listener);
    listener = null;
  }

  public Collection<UsableTrigger> getTriggers() {
    return Collections.unmodifiableCollection(triggers.values());
  }

  /**
   * Adds the given trigger to this manager
   *
   * @param trigger The trigger to add
   * @return True, if trigger was not already contained by this manager
   */
  public boolean add(UsableTrigger trigger) {
    if (triggers.containsKey(trigger.getName())) {
      return false;
    }

    triggers.put(trigger.getName(), trigger);
    worldMap.add(trigger.getBounds().getWorld(), trigger);

    return true;
  }

  /**
   * Gets a trigger by its name
   *
   * @param name The name of the trigger
   * @return The found trigger, null, if no trigger was found with the given name
   */
  public UsableTrigger getNamed(String name) {
    return triggers.get(name);
  }

  /**
   * Checks if this manager contains a trigger with the given name
   *
   * @param name The trigger's name
   * @return True, if this manager has the trigger
   */
  public boolean contains(String name) {
    return getNamed(name) != null;
  }

  /**
   * Removes the given trigger from this manager
   *
   * @param trigger The trigger to remove
   * @return True, if this manager contained this trigger and it was removed
   */
  public boolean remove(UsableTrigger trigger) {
    if (triggers.remove(trigger.getName()) == null) {
      return false;
    }

    worldMap.remove(trigger.getBounds().getWorld(), trigger);
    return true;
  }

  public void run(Player player, Location source, Location destination) {
    Bounds3i sourceBounds = makePlayerBounds(source);
    Bounds3i destBounds = makePlayerBounds(destination);

    World sourceWorld = source.getWorld();
    World destWorld = destination.getWorld();

    if (Objects.equals(sourceWorld, destWorld)) {
      Bounds3i totalArea = sourceBounds.combine(destBounds);

      var triggers = this.worldMap.getOverlapping(sourceWorld, totalArea);

      if (triggers.isEmpty()) {
        return;
      }

      triggers.forEach(trigger -> {
        var type = trigger.getType();

        if (!type.shouldRun(trigger.getBounds(), sourceBounds, destBounds)) {
          return;
        }

        trigger.interact(player);
      });

      return;
    }

    Set<UsableTrigger> sourceTriggers
        = this.worldMap.getOverlapping(sourceWorld, sourceBounds);

    Set<UsableTrigger> destTriggers
        = this.worldMap.getOverlapping(destWorld, destBounds);

    if (!sourceTriggers.isEmpty()) {
      sourceTriggers.forEach(trigger -> {
        if (trigger.getType() == TriggerType.ENTER) {
          return;
        }

        trigger.interact(player);
      });
    }

    if (!destTriggers.isEmpty()) {
      destTriggers.forEach(trigger -> {
        if (trigger.getType() == TriggerType.EXIT) {
          return;
        }

        trigger.interact(player);
      });
    }
  }

  public static Bounds3i makePlayerBounds(Location l) {
    return Bounds3i.of(
        l.getX() - PLAYER_HALF_WIDTH,
        l.getY(),
        l.getZ() - PLAYER_HALF_WIDTH,

        l.getX() + PLAYER_HALF_WIDTH,
        l.getY() + PLAYER_HEIGHT,
        l.getZ() + PLAYER_HALF_WIDTH
    );
  }

  /**
   * Clears this manager
   */
  public void clear() {
    triggers.clear();
    worldMap.clear();
  }

  /**
   * Saves all the triggers in this manager into a single list
   */
  public void save(CompoundTag tag) {
    for (var v : triggers.values()) {
      var triggerTag = new CompoundTag();
      v.save(triggerTag);

      tag.put(v.getName(), triggerTag);
    }
  }

  /**
   * Loads all triggers from the given list
   *
   * @param lTag The list to load from
   */
  public void load(CompoundTag lTag) {
    clear();

    for (var t : lTag.tags.entrySet()) {
      var name = t.getKey();
      var tag = (CompoundTag) t.getValue();

      add(new UsableTrigger(name, tag));
    }
  }

  /**
   * Gets the names of all triggers in this manager
   *
   * @return All trigger names
   */
  public Set<String> getNames() {
    return triggers.keySet();
  }

  /**
   * Gets the size of this manager
   *
   * @return The amount of triggers held by this manager
   */
  public int size() {
    return triggers.size();
  }

  /**
   * Checks if this manager is empty
   *
   * @return True, if {@link #size()} <= 0
   */
  public boolean isEmpty() {
    return triggers.isEmpty();
  }
}