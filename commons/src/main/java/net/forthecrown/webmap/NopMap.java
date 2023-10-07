package net.forthecrown.webmap;

import java.io.InputStream;
import java.util.Optional;
import net.forthecrown.utils.Result;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

class NopMap implements WebMap {

  static final NopMap NOP = new NopMap();

  @Override
  public Optional<MapLayer> getLayer(@NotNull World world, String id) {
    return Optional.empty();
  }

  @Override
  public Result<MapLayer> createLayer(@NotNull World world, String id, String name) {
    return Result.error("NO-OP implementation (No FTC-Webmap installed)");
  }

  @Override
  public Optional<MapIcon> getIcon(String id) {
    return Optional.empty();
  }

  @Override
  public Result<MapIcon> createIcon(String id, String name, InputStream iconData) {
    return Result.error("NO-OP implementation (No FTC-Webmap installed)");
  }

  @Override
  public boolean isPlayerVisible(OfflinePlayer player) {
    return false;
  }

  @Override
  public void setPlayerVisible(OfflinePlayer player, boolean visible) {

  }
}
