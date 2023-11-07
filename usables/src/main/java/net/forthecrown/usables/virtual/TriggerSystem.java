package net.forthecrown.usables.virtual;

public interface TriggerSystem<T extends Trigger> {

  default void initializeSystem(VirtualUsableManager manager) {

  }

  void onTriggerLoaded(VirtualUsable usable, T trigger);

  void onTriggerAdd(VirtualUsable usable, T trigger);

  default void onTriggerUnload(VirtualUsable usable, T trigger) {
    onTriggerRemove(usable, trigger);
  }

  void onTriggerRemove(VirtualUsable usable, T trigger);
}
