package net.forthecrown.dungeons.level.room;

import java.util.Optional;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.core.registry.RegistryIndex;

public class RoomComponents {
  public static final Registry<RoomComponentType> REGISTRY;
  public static final RegistryIndex<RoomComponentType, Class> INDEX;

  static {
    INDEX = new RegistryIndex<>(holder -> holder.getValue().getType());
    REGISTRY = Registries.newRegistry();
    REGISTRY.setIndex(INDEX);
  }

  public static <T extends RoomComponent> RoomComponentType<T> of(Class<T> type) {
    Optional<RoomComponentType> opt = INDEX.lookupValue(type);

    if (opt.isPresent()) {
      return opt.get();
    }

    String filteredName = filterName(type);
    RoomComponentType<T> cType = new RoomComponentType<>(type);
    var holder = REGISTRY.register(filteredName, cType);
    cType.onRegister(holder.getKey(), holder.getId());

    return cType;
  }

  private static String filterName(Class cName) {
    String name = cName.getSimpleName().replaceAll("Component", "");
    return name.substring(0, 1).toLowerCase() + name.substring(1);
  }
}