package net.forthecrown.usables.trigger;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.usables.trigger.AreaTrigger.Type;
import net.forthecrown.usables.virtual.RegionAction;
import net.forthecrown.usables.virtual.Triggers;
import net.forthecrown.utils.collision.CollisionListener;
import net.forthecrown.utils.collision.CollisionSystem;
import net.forthecrown.utils.collision.CollisionSystems;
import net.forthecrown.utils.collision.WorldChunkMap;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.entity.Player;

public class TriggerManager {

  private final Path file;

  private final Map<String, AreaTrigger> triggers = new Object2ObjectOpenHashMap<>();

  @Getter
  private final CollisionSystem<Player, AreaTrigger> collisionSystem;
  private final WorldChunkMap<AreaTrigger> chunkMap;

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

      AreaTrigger trigger = new AreaTrigger();
      trigger.setName(name);
      trigger.load(tag);

      add(trigger);
    }
  }

  public void remove(AreaTrigger trigger) {
    if (triggers.remove(trigger.getName()) == null) {
      return;
    }

    trigger.manager = null;
    chunkMap.remove(trigger.getArea().getWorld(), trigger);

    if (triggers.isEmpty()) {
      collisionSystem.stopListening();
    }
  }

  public void add(AreaTrigger trigger) {
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

  public AreaTrigger get(String name) {
    return triggers.get(name);
  }

  public Set<String> getNames() {
    return triggers.keySet();
  }

  private static class TriggerCollisions implements CollisionListener<Player, AreaTrigger> {

    private static final Set<Type> VALID_ENTER_TYPES = Set.of(Type.ENTER, Type.EITHER, Type.MOVE);
    private static final Set<Type> VALID_EXIT_TYPES = Set.of(Type.EXIT, Type.EITHER, Type.MOVE);

    private static final Set<RegionAction> ON_ENTER = Set.of(
        RegionAction.ON_REGION_ENTER,
        RegionAction.ON_REGION_ENTER_EXIT,
        RegionAction.ON_REGION_MOVE_INSIDE_OF
    );

    private static final Set<RegionAction> ON_EXIT = Set.of(
        RegionAction.ON_REGION_EXIT,
        RegionAction.ON_REGION_ENTER_EXIT,
        RegionAction.ON_REGION_MOVE_INSIDE_OF
    );

    private static final Set<RegionAction> ON_MOVE_INSIDE = Set.of(
        RegionAction.ON_REGION_MOVE_INSIDE_OF
    );

    private void runExternal(AreaTrigger trigger, Player player, Set<RegionAction> actions) {
      var refs = trigger.getExternalTriggers().getAll(actions);

      if (refs.isEmpty()) {
        return;
      }

      Triggers.runReferences(
          refs, player, null,
          interaction -> {
            var ctx = interaction.context();
            ctx.put("triggerName", trigger.getName());
            ctx.put("area", trigger.getArea());
          },
          null
      );
    }

    @Override
    public void onEnter(Player source, AreaTrigger trigger) {
      if (VALID_ENTER_TYPES.contains(trigger.getType())) {
        trigger.interact(source);
      }

      runExternal(trigger, source, ON_ENTER);
    }

    @Override
    public void onExit(Player source, AreaTrigger trigger) {
      if (VALID_EXIT_TYPES.contains(trigger.getType())) {
        trigger.interact(source);
      }

      runExternal(trigger, source, ON_EXIT);
    }

    @Override
    public void onMoveInside(Player source, AreaTrigger trigger) {
      if (trigger.getType() == Type.MOVE) {
        trigger.interact(source);
      }

      runExternal(trigger, source, ON_MOVE_INSIDE);
    }
  }
}
