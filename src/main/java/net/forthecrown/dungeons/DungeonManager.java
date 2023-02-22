package net.forthecrown.dungeons;

import lombok.Getter;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.dungeons.level.PieceType;
import net.forthecrown.dungeons.level.gate.GateType;
import net.forthecrown.dungeons.level.room.RoomType;
import net.forthecrown.utils.io.PathUtil;

@Getter
public class DungeonManager {

  @Getter
  private static final DungeonManager dungeons = new DungeonManager();

  @SuppressWarnings("rawtypes")
  private final Registry<Registry<PieceType>>
      typeRegistries = Registries.newRegistry();

  private final Registry<RoomType>
      roomTypes = createTypeRegistry("rooms");

  private final Registry<GateType>
      gateTypes = createTypeRegistry("gates");

  private final LevelDataStorage storage;

  private DungeonManager() {
    this.storage = new LevelDataStorage(
        PathUtil.getPluginDirectory("dungeons")
    );
  }

  @OnLoad
  public void reload() {
    roomTypes.clear();
    gateTypes.clear();

    storage.loadGates(gateTypes);
    storage.loadRooms(roomTypes);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private <T extends PieceType<?>> Registry<T> createTypeRegistry(String name) {
    Registry registry = Registries.newRegistry();
    typeRegistries.register(name, registry);
    return registry;
  }
}