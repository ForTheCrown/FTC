package net.forthecrown.dungeons;

import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.function.Function;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.dungeons.level.PieceType;
import net.forthecrown.dungeons.level.gate.GateType;
import net.forthecrown.dungeons.level.room.RoomType;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.StructureFillConfig;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

@Getter
public class LevelDataStorage {

  private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
      .appendValue(ChronoField.YEAR)
      .appendLiteral('_')
      .appendValue(ChronoField.MONTH_OF_YEAR, 2)
      .appendLiteral('_')
      .appendValue(ChronoField.DAY_OF_MONTH, 2)
      .appendLiteral('_')
      .appendValue(ChronoField.HOUR_OF_DAY)
      .appendLiteral('_')
      .appendValue(ChronoField.MINUTE_OF_HOUR)
      .toFormatter();

  private static final Logger LOGGER = Loggers.getLogger();

  private final Path directory;
  private final Path archiveDirectory;

  private final Path activeLevel;

  private final Path roomJson;
  private final Path gateJson;

  LevelDataStorage(Path directory) {
    this.directory = directory;
    this.archiveDirectory = directory.resolve("archives");
    this.activeLevel = directory.resolve("level.dat");

    this.roomJson = directory.resolve("rooms.json");
    this.gateJson = directory.resolve("gates.json");

    saveDefaults();
  }

  void saveDefaults() {
    try {
      FtcJar.saveResources(
          "dungeons",
          directory,
          ALLOW_OVERWRITE | OVERWRITE_IF_NEWER
      );
    } catch (IOException exc) {
      LOGGER.error("Couldn't save default dungeon files", exc);
    }
  }

  public Path getPath(long creationTime, int i) {
    LocalDateTime localTime = Time.localTime(creationTime);
    String strPath = FORMATTER.format(localTime);

    if (i > 0) {
      strPath += " (" + i + ")";
    }

    strPath += ".dat";
    return archiveDirectory.resolve(strPath);
  }

  public void archiveLevelStructure(DungeonLevel level, long creationTime) {
    Path path = null;
    int i = 0;

    while (path == null || Files.exists(path)) {
      path = getPath(creationTime, i);
    }

    BlockStructure structure = new BlockStructure();
    StructureFillConfig config = StructureFillConfig.builder()
        .blockPredicate(block -> !block.getType().isAir())
        .area(level.getChunkMap().getTotalArea().toWorldBounds(DungeonWorld.get()))
        .build();

    structure.fill(config);

    var header = structure.getHeader();
    header.putString("createdDate", JsonUtils.DATE_FORMAT.format(new Date(creationTime)));

    CompoundTag levelData = BinaryTags.compoundTag();
    level.save(levelData);
    header.put("level_data", levelData);

    SerializationHelper.writeTagFile(path, structure::save);
  }

  public void loadRooms(Registry<RoomType> roomTypeRegistry) {
    loadPieceTypes(getRoomJson(), roomTypeRegistry, RoomType::loadType);
  }

  public void loadGates(Registry<GateType> gateTypeRegistry) {
    loadPieceTypes(getGateJson(), gateTypeRegistry, GateType::loadType);
  }

  public <T extends PieceType<?>> void loadPieceTypes(
      Path path,
      Registry<T> registry,
      Function<JsonWrapper, DataResult<T>> function
  ) {
    SerializationHelper.readJsonFile(path, wrapper -> {
      for (var e: wrapper.entrySet()) {
        var key = e.getKey();

        if (!Registries.isValidKey(key)) {
          LOGGER.error("Cannot read piece type! Invalid key '{}'", key);
          continue;
        }

        if (!e.getValue().isJsonObject()) {
          LOGGER.error("Cannot read type '{}', not a JSON object", key);
          continue;
        }

        var obj = e.getValue().getAsJsonObject();

        function.apply(JsonWrapper.wrap(obj))
            .mapError(s -> "Cannot read type '" + key + "', " + s)
            .resultOrPartial(LOGGER::error)
            .ifPresent(t -> {
              registry.register(key, t);
            });
      }
    });
  }
}