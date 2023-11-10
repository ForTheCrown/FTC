package net.forthecrown.webmap.bluemap;

import com.mojang.datafixers.util.Unit;
import de.bluecolored.bluemap.api.AssetStorage;
import java.io.IOException;
import net.forthecrown.Loggers;
import net.forthecrown.utils.Result;
import net.forthecrown.webmap.MapIcon;
import org.slf4j.Logger;

public class BlueMapIcon implements MapIcon {

  private static final Logger LOGGER = Loggers.getLogger();

  final String id;
  final String path;
  final IconIndex index;
  final AssetStorage storage;

  public BlueMapIcon(String id, String path, IconIndex index) {
    this.id = id;
    this.path = path;
    this.index = index;
    this.storage = null;
  }

  public BlueMapIcon(String path, AssetStorage storage) {
    this.id = path.replace(".png", "");
    this.path = path;
    this.index = null;
    this.storage = storage;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Result<Unit> setName(String name) {
    return Result.error("Not-implemented");
  }

  @Override
  public void delete() {
    if (index == null) {
      try {
        storage.deleteAsset(path);
      } catch (IOException exc) {
        LOGGER.error("Error deleting icon asset", exc);
      }
    } else {
      index.deleteIcon(id);
    }
  }
}
