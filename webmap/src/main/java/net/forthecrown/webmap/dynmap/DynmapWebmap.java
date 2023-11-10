package net.forthecrown.webmap.dynmap;

import com.google.common.base.Strings;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import net.forthecrown.utils.Result;
import net.forthecrown.webmap.MapIcon;
import net.forthecrown.webmap.MapLayer;
import net.forthecrown.webmap.WebMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.MarkerIcon;
import org.jetbrains.annotations.NotNull;

public class DynmapWebmap implements WebMap {

  private DynmapCommonAPI dynmap;

  public DynmapWebmap() {

  }

  static String getId(World world) {
    return world.getName();
  }

  static String getId(OfflinePlayer player) {
    return player.getName();
  }

  static MarkerIcon getIcon(MapIcon icon) {
    if (icon instanceof DynmapIcon ico) {
      return ico.icon;
    }
    return null;
  }

  private DynmapCommonAPI getDynmap() {
    if (dynmap != null) {
      return dynmap;
    }

    Plugin plugin = Bukkit.getPluginManager().getPlugin("dynmap");

    if (plugin == null) {
      return null;
    }

    return dynmap = (DynmapCommonAPI) plugin;
  }

  private Optional<DynmapCommonAPI> apiOptional() {
    return Optional.ofNullable(getDynmap());
  }

  private Result<DynmapCommonAPI> apiResult() {
    var api = getDynmap();

    if (api == null) {
      return Result.error("No Dynmap API present (???)");
    }

    return Result.success(api);
  }

  @Override
  public Optional<MapLayer> getLayer(@NotNull World world, String id) {
    if (world == null || Strings.isNullOrEmpty(id)) {
      return Optional.empty();
    }

    return apiOptional()
        .map(DynmapCommonAPI::getMarkerAPI)
        .map(markers -> markers.getMarkerSet(id))
        .map(markerSet -> new DynmapLayer(markerSet, world));
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

    return apiResult().flatMap(api -> {
      var markers = api.getMarkerAPI();
      var existing = markers.getMarkerSet(id);

      if (existing != null) {
        return Result.error("Layer with ID '%s' already exists", id);
      }

      var set = markers.createMarkerSet(id, name, null, false);
      return Result.success(new DynmapLayer(set, world));
    });
  }

  @Override
  public Optional<MapIcon> getIcon(String id) {
    if (Strings.isNullOrEmpty(id)) {
      return Optional.empty();
    }

    return apiOptional()
        .map(DynmapCommonAPI::getMarkerAPI)
        .map(markerAPI -> markerAPI.getMarkerIcon(id))
        .map(DynmapIcon::new);
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

    return apiResult().flatMap(api -> {
      var markers = api.getMarkerAPI();
      var existing = markers.getMarkerIcon(id);

      if (existing != null) {
        return Result.error("Icon with ID '%s' already exists", id);
      }

      var icon = markers.createMarkerIcon(id, name, iconData);
      return Result.success(new DynmapIcon(icon));
    });
  }

  @Override
  public boolean isPlayerVisible(OfflinePlayer player) {
    Objects.requireNonNull(player, "Null player");
    return apiOptional().map(api -> api.getPlayerVisbility(getId(player))).orElse(false);
  }

  @Override
  public void setPlayerVisible(OfflinePlayer player, boolean visible) {
    Objects.requireNonNull(player, "Null player");
    apiOptional().ifPresent(api -> api.setPlayerVisiblity(getId(player), visible));
  }
}
