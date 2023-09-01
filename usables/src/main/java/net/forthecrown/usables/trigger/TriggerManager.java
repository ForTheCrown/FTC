package net.forthecrown.usables.trigger;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.usables.trigger.Trigger.Type;
import net.forthecrown.utils.collision.CollisionListener;
import net.forthecrown.utils.collision.CollisionSystem;
import net.forthecrown.utils.collision.CollisionSystems;
import net.forthecrown.utils.collision.WorldChunkMap;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.entity.Player;

public class TriggerManager {

  private final Path file;

  private final Map<String, Trigger> triggers = new Object2ObjectOpenHashMap<>();

  @Getter
  private final CollisionSystem<Player, Trigger> collisionSystem;
  private final WorldChunkMap<Trigger> chunkMap;

  public TriggerManager(Path file) {
    this.chunkMap = new WorldChunkMap<>();
    this.collisionSystem = CollisionSystems.createSystem(chunkMap, new TriggerCollisions());
    this.file = file;
  }

  public void clear() {
    triggers.clear();
    chunkMap.clear();
    collisionSystem.stopListening();
  }

  public void save() {
    SerializationHelper.writeTagFile(file, this::save);
  }

  public void load() {
    SerializationHelper.readTagFile(file, this::load);
  }

  public void save(CompoundTag tag) {
    for (var v : triggers.values()) {
      var triggerTag = BinaryTags.compoundTag();
      v.save(triggerTag);

      tag.put(v.getName(), triggerTag);
    }
  }

  public void load(CompoundTag lTag) {
    clear();

    for (var t : lTag.entrySet()) {
      var name = t.getKey();
      var tag = (CompoundTag) t.getValue();

      Trigger trigger = new Trigger();
      trigger.setName(name);
      trigger.load(tag);

      add(trigger);
    }
  }

  public void remove(Trigger trigger) {
    if (triggers.remove(trigger.getName()) == null) {
      return;
    }

    trigger.manager = null;
    chunkMap.remove(trigger.getArea().getWorld(), trigger);

    if (triggers.isEmpty()) {
      collisionSystem.stopListening();
    }
  }

  public void add(Trigger trigger) {
    if (Strings.isNullOrEmpty(trigger.getName())) {
      throw new IllegalArgumentException("Attempted to add Trigger with empty name");
    }

    remove(trigger);

    triggers.put(trigger.getName(), trigger);
    chunkMap.add(trigger.getArea(), trigger);

    trigger.manager = this;

    if (!collisionSystem.isListenerRegistered()) {
      collisionSystem.beginListening();
    }
  }

  public Trigger get(String name) {
    return triggers.get(name);
  }

  public Set<String> getNames() {
    return triggers.keySet();
  }

  private static class TriggerCollisions implements CollisionListener<Player, Trigger> {

    private static final Set<Type> VALID_ENTER_TYPES = Set.of(Type.ENTER, Type.EITHER, Type.MOVE);
    private static final Set<Type> VALID_EXIT_TYPES = Set.of(Type.EXIT, Type.EITHER, Type.MOVE);

    @Override
    public void onEnter(Player source, Trigger trigger) {
      if (!VALID_ENTER_TYPES.contains(trigger.getType())) {
        return;
      }

      trigger.interact(source);
    }

    @Override
    public void onExit(Player source, Trigger trigger) {
      if (!VALID_EXIT_TYPES.contains(trigger.getType())) {
        return;
      }

      trigger.interact(source);
    }

    @Override
    public void onMoveInside(Player source, Trigger trigger) {
      if (trigger.getType() != Type.MOVE) {
        return;
      }

      trigger.interact(source);
    }
  }
}
