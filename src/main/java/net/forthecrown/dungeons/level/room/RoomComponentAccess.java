package net.forthecrown.dungeons.level.room;

import java.util.List;

public interface RoomComponentAccess {
  boolean addComponent(RoomComponent component);

  default <T extends RoomComponent> T removeComponent(Class<T> type) {
    return removeComponent(RoomComponents.of(type));
  }

  <T extends RoomComponent> T removeComponent(RoomComponentType<T> type);

  default boolean hasComponent(Class<? extends RoomComponent> component) {
    return getComponent(component) != null;
  }

  default boolean hasComponent(RoomComponentType type) {
    return getComponent(type) != null;
  }

  <T> List<T> getInheritedComponents(Class<T> type);

  default <T extends RoomComponent> T getComponent(Class<T> type) {
    return getComponent(RoomComponents.of(type));
  }

  <T extends RoomComponent> T getComponent(RoomComponentType<T> type);
}