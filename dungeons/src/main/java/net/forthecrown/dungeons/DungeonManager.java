package net.forthecrown.dungeons;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.dungeons.level.PieceType;
import net.forthecrown.dungeons.level.gate.GateType;
import net.forthecrown.dungeons.level.room.RoomType;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.io.PathUtil;
import org.slf4j.Logger;

@Getter
public class DungeonManager {

  private static final Logger LOGGER = Loggers.getLogger();

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

  private final ExecutorService executorService
      = Executors.newCachedThreadPool(createThreadFactory());

  private DungeonManager() {
    this.storage = new LevelDataStorage(
        PathUtil.ensureDirectoryExists(PathUtil.pluginPath("dungeons"))
    );
  }

  private static ThreadFactory createThreadFactory() {
    return r -> {
      Thread t = new Thread(r);
      t.setUncaughtExceptionHandler((t1, e) -> {
        LOGGER.error("Error running thread '{}'", t1.getName(), e);
      });
      return t;
    };
  }

  public void reload() {
    roomTypes.clear();
    gateTypes.clear();

    storage.loadGates(gateTypes);
    storage.loadRooms(roomTypes);
  }

  public void shutdown() {
    executorService.shutdownNow();
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private <T extends PieceType<?>> Registry<T> createTypeRegistry(String name) {
    Registry registry = Registries.newRegistry();
    typeRegistries.register(name, registry);
    return registry;
  }
}