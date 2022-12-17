package net.forthecrown.dungeons;

import static net.forthecrown.utils.io.FtcJar.ALLOW_OVERWRITE;
import static net.forthecrown.utils.io.FtcJar.OVERWRITE_IF_NEWER;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.dungeons.level.PieceStyle;
import net.forthecrown.dungeons.level.Pieces;
import net.forthecrown.dungeons.level.RoomType;
import net.forthecrown.dungeons.level.gate.GateType;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.utils.Util;
import net.forthecrown.utils.io.FtcJar;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

@Getter
public class DungeonDataStorage {

  private static final Logger LOGGER = FTC.getLogger();

  private static final String
      KEY_STRUCTURE = "structure",
      KEY_VARIANTS = "variants",
      KEY_OPTIONS = "options",
      KEY_CAN_OPEN = "canOpen",
      KEY_PALETTES = "palettes";

  private final Path directory;

  private final Path roomsJson;
  private final Path gatesJson;

  public DungeonDataStorage(Path directory) {
    this.directory = directory;

    this.roomsJson = directory.resolve("rooms.json");
    this.gatesJson = directory.resolve("gates.json");

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

  void loadRooms(Registry<RoomType> types) {
    SerializationHelper.readJsonFile(getRoomsJson(), wrapper -> {
      int loaded = 0;

      for (var e : wrapper.entrySet()) {
        if (!Keys.isValidKey(e.getKey())) {
          LOGGER.warn("Invalid key for room type: '{}'", e.getKey());
          continue;
        }

        if (!e.getValue().isJsonObject()) {
          LOGGER.warn(
              "Entry {} didn't have an object for a " +
                  "value, cannot read",
              e.getKey()
          );

          continue;
        }

        JsonWrapper json = JsonWrapper.wrap(
            e.getValue().getAsJsonObject()
        );

        String struct = json.getString(KEY_STRUCTURE);

        if (Strings.isNullOrEmpty(struct)) {
          LOGGER.error("No structure name given for {}", e.getKey());
          continue;
        }

        var variants = readVariants(json.get(KEY_VARIANTS));
        int flags = readRoomFlags(json.get(KEY_OPTIONS));

        RoomType type = new RoomType(struct, variants, flags);
        types.register(e.getKey(), type);

        ++loaded;
      }

      LOGGER.debug("Loaded {} room types", loaded);
    });
  }

  void loadGates(Registry<GateType> gateTypes) {
    SerializationHelper.readJsonFile(getRoomsJson(), wrapper -> {
      int loaded = 0;

      for (var e : wrapper.entrySet()) {
        if (!Keys.isValidKey(e.getKey())) {
          LOGGER.warn("Invalid key for gate type: '{}'", e.getKey());
          continue;
        }

        if (!e.getValue().isJsonObject()) {
          LOGGER.warn(
              "Entry {} didn't have an object for a " +
                  "value, cannot read",
              e.getKey()
          );

          continue;
        }

        JsonWrapper json = JsonWrapper.wrap(
            e.getValue().getAsJsonObject()
        );

        String struct = json.getString(KEY_STRUCTURE);

        if (Strings.isNullOrEmpty(struct)) {
          LOGGER.error("No structure name given for {}", e.getKey());
          continue;
        }

        var variants = readVariants(json.get(KEY_VARIANTS));

        String openPallette = null;
        String closedPallette = BlockStructure.DEFAULT_PALETTE_NAME;

        if (json.has(KEY_PALETTES)) {
          var palettes = json.getWrapped(KEY_PALETTES);
          openPallette = palettes.getString("open");
          closedPallette = palettes.getString("closed");
        }

        GateType type = new GateType(
            struct, variants,
            openPallette,
            closedPallette
        );
        gateTypes.register(e.getKey(), type);

        ++loaded;
      }

      LOGGER.debug("Loaded {} gate types", loaded);
    });
  }

  private static int readRoomFlags(JsonElement element) {
    if (element == null || element.isJsonNull()) {
      return 0;
    }

    if (element.isJsonPrimitive()) {
      var prim = element.getAsJsonPrimitive();
      return readFlag(prim);
    }

    JsonArray array = element.getAsJsonArray();
    int result = 0;

    for (var e : array) {
      result |= readFlag(e.getAsJsonPrimitive());
    }

    return result;
  }

  private static int readFlag(JsonPrimitive prim) {
    if (prim.isNumber()) {
      return prim.getAsInt();
    }

    var flag = Pieces.FLAGS_BY_NAME.get(prim.toString());

    if (flag == null) {
      throw Util.newException(
          "Unknown room option: '%s'",
          prim.toString()
      );
    }

    return flag;
  }

  private static Map<PieceStyle, String> readVariants(JsonElement element) {
    if (element == null || !element.isJsonObject()) {
      return Collections.emptyMap();
    }

    Map<PieceStyle, String> map = new Object2ObjectOpenHashMap<>();

    for (var e : element.getAsJsonObject().entrySet()) {
      PieceStyle variant = PieceStyle.valueOf(e.getKey().toUpperCase());
      map.put(variant, e.getValue().getAsString());
    }

    return map;
  }
}