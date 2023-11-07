package net.forthecrown.webmap;

import java.io.InputStream;
import java.util.Optional;
import net.forthecrown.BukkitServices;
import net.forthecrown.utils.Result;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract interface for representing an API that interacts with a web-based map like BlueMap or
 * Dynmap
 */
public interface WebMap {

  static WebMap map() {
    return BukkitServices.load(WebMap.class).orElse(NopMap.NOP);
  }

  Optional<MapLayer> getLayer(@NotNull World world, String id);

  Result<MapLayer> createLayer(@NotNull World world, String id, String name);

  Optional<MapIcon> getIcon(String id);

  Result<MapIcon> createIcon(String id, String name, InputStream iconData);

  boolean isPlayerVisible(OfflinePlayer player);

  void setPlayerVisible(OfflinePlayer player, boolean visible);
}
