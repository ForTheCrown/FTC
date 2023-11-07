package net.forthecrown.usables.virtual;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Objects;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.usables.ObjectType;

public class VirtualUsableManager {

  private final Registry<ObjectType<? extends Trigger>> registry = Registries.newRegistry();

  private final Map<String, VirtualUsable> usableMap = new Object2ObjectOpenHashMap<>();

  private final Map<Class, TriggerSystem> triggerSystems = new Object2ObjectOpenHashMap<>();
  private boolean systemsLocked;

  public void initialize() {
    Triggers.registerAll(registry);
    Triggers.manager = this;

    addSystem(BlockTrigger.class, new BlockTriggerSystem());
    addSystem(EntityTrigger.class, new EntityTriggerSystem());
    addSystem(RegionTrigger.class, new RegionTriggerSystem());

    lockSystems();
  }

  public void lockSystems() {
    systemsLocked = true;
  }

  void ensureSystemsUnlocked() {
    Preconditions.checkState(!systemsLocked, "System adding/removing has been locked");
  }

  public <T extends Trigger> void addSystem(Class<T> type, TriggerSystem<T> system) {
    Objects.requireNonNull(system, "Null system");
    ensureSystemsUnlocked();

    if (triggerSystems.containsKey(type)) {
      throw new IllegalStateException("System under class " + type + " already registered");
    }

    triggerSystems.put(type, system);
    system.initializeSystem(this);
  }

  public <T extends Trigger> TriggerSystem<T> getSystem(Class<T> type) {
    return triggerSystems.get(type);
  }

  public VirtualUsable getUsable(String name) {
    return usableMap.get(name);
  }

  void internalAdd(VirtualUsable usable) {
    Objects.requireNonNull(usable, "Null usable");

    if (usableMap.containsKey(usable.getName())) {
      throw new IllegalStateException(
          "Usable with name '" + usable.getName() + "' is already registered"
      );
    }

    usableMap.put(usable.getName(), usable);
    usable.manager = this;
  }

  public void add(VirtualUsable usable) {
    internalAdd(usable);
  }

  VirtualUsable internalRemove(String name) {
    Objects.requireNonNull(name, "Null name");

    var removed = usableMap.remove(name);
    if (removed != null) {
      removed.manager = null;
    }

    return removed;
  }

  public void remove(String name) {
    Objects.requireNonNull(name, "Null name");
    var removed = usableMap.get(name);

    if (removed == null) {
      return;
    }

    usableMap.remove(name);
    removed.manager = null;
  }

  public boolean containsUsable(String name) {
    return usableMap.containsKey(name);
  }
}
