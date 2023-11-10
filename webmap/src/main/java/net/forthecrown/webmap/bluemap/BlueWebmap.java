package net.forthecrown.webmap.bluemap;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import net.forthecrown.Loggers;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.webmap.MapIcon;
import net.forthecrown.webmap.MapLayer;
import net.forthecrown.webmap.WebMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class BlueWebmap implements WebMap {

  private static final Logger LOGGER = Loggers.getLogger();

  private final IconIndex iconIndex;

  private final Map<String, String> pluginSets = new Object2ObjectOpenHashMap<>();
  private final Path markerJson;

  public BlueWebmap(Path pluginDir) {
    this.iconIndex = new IconIndex(pluginDir.resolve("bluemap-icons"));
    this.markerJson = pluginDir.resolve("markers.json");

    BlueMapAPI.onEnable(blueMapAPI -> load());
  }

  private Optional<BlueMapAPI> api() {
    return BlueMapAPI.getInstance();
  }

  private Optional<BlueMapMap> getMap(Object world) {
    if (world == null) {
      return Optional.empty();
    }

    return api()
        .flatMap(blueMapAPI -> blueMapAPI.getWorld(world))
        .map(w -> {
          var maps = w.getMaps();
          if (maps.size() != 1) {
            return (BlueMapMap) null; // Don't let IntelliJ fool you, this cast is required
          }
          return maps.iterator().next();
        });
  }

  @Override
  public Optional<MapLayer> getLayer(@NotNull World world, String id) {
    if (world == null || Strings.isNullOrEmpty(id)) {
      return Optional.empty();
    }

    return getMap(world)
        .map(blueMapMap -> {
          MarkerSet set = blueMapMap.getMarkerSets().get(id);

          if (set == null) {
            return null;
          }

          return new BlueMapLayer(id, set, world, blueMapMap, this);
        });
  }

  @Override
  public Result<MapLayer> createLayer(@NotNull World world, String id, String name) {
    if (Strings.isNullOrEmpty(id)) {
      return Result.error("Null/empty ID");
    }
    if (Strings.isNullOrEmpty(name)) {
      return Result.error("Null/empty layer name");
    }
    if (world == null) {
      return Result.error("Null world");
    }
    var opt = getMap(world);

    if (opt.isEmpty()) {
      return Result.error("BlueMap does not have the '%s' world", world.getName());
    }

    var mapMap = opt.get();
    var existing = mapMap.getMarkerSets().get(id);

    if (existing != null) {
      return Result.error("Layer with ID '%s' is already defined", id);
    }

    var set = new MarkerSet(name);
    pluginSets.put(id, world.getName());

    return Result.success(new BlueMapLayer(id, set, world, mapMap, this));
  }

  @Override
  public Optional<MapIcon> getIcon(String id) {
    if (Strings.isNullOrEmpty(id)) {
      return Optional.empty();
    }

    return Optional.ofNullable(iconIndex.getIconPath(id))
        .filter(Files::exists)
        .map(path -> new BlueMapIcon(id, path.toString(), iconIndex));
  }

  @Override
  public Result<MapIcon> createIcon(String id, String name, InputStream iconData) {
    if (Strings.isNullOrEmpty(id)) {
      return Result.error("Null/empty ID");
    }
    if (Strings.isNullOrEmpty(name)) {
      return Result.error("Null/empty icon name");
    }
    if (iconData == null) {
      return Result.error("Null icon-data");
    }

    return iconIndex.createIconFile(id, iconData)
        .map(path -> new BlueMapIcon(id, path.toString(), iconIndex));
  }

  @Override
  public boolean isPlayerVisible(OfflinePlayer player) {
    Objects.requireNonNull(player, "Null player");
    return api().map(a -> a.getWebApp().getPlayerVisibility(player.getUniqueId())).orElse(false);
  }

  @Override
  public void setPlayerVisible(OfflinePlayer player, boolean visible) {
    Objects.requireNonNull(player, "Null player");
    api().ifPresent(api -> api.getWebApp().setPlayerVisibility(player.getUniqueId(), visible));
  }

  public void save() {
    SerializationHelper.writeJsonFile(markerJson, json -> {
      for (Entry<String, String> entry : pluginSets.entrySet()) {
        getMap(entry.getValue()).ifPresent(mapMap -> {
          MarkerSet set = mapMap.getMarkerSets().get(entry.getKey());

          if (set == null) {
            return;
          }

          JsonElement serialized = MarkerGson.INSTANCE.toJsonTree(set);
          JsonWrapper markerJson = JsonWrapper.create();
          markerJson.add("world", entry.getValue());
          markerJson.add("marker_data", serialized);

          json.add(entry.getKey(), markerJson);
        });
      }
    });
  }

  public void load() {
    SerializationHelper.readAsJson(markerJson, jsonWrapper -> {
      for (Entry<String, JsonElement> entry : jsonWrapper.entrySet()) {
        JsonWrapper json = JsonWrapper.wrap(entry.getValue().getAsJsonObject());
        String worldName = json.getString("world");
        JsonElement data = json.get("marker_data");

        MarkerSet set = MarkerGson.INSTANCE.fromJson(data, MarkerSet.class);

        getMap(worldName).ifPresent(mapMap -> {
          mapMap.getMarkerSets().put(entry.getKey(), set);
          pluginSets.put(entry.getKey(), worldName);
        });
      }
    });
  }
}
