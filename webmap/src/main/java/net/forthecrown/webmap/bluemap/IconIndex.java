package net.forthecrown.webmap.bluemap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import net.forthecrown.Loggers;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.PathUtil;

public class IconIndex {

  private final Path directory;

  public IconIndex(Path directory) {
    this.directory = directory;
    PathUtil.ensureDirectoryExists(directory);
  }

  public Path getIconPath(String id) {
    return directory.resolve(id + (id.endsWith(".png") ? "" : ".png"));
  }

  public Result<Path> createIconFile(String id, InputStream stream) {
    Path file = getIconPath(id);

    if (Files.exists(file)) {
      return Result.error("Icon with ID '%s' already exists", id);
    }

    try {
      Files.copy(stream, file);
    } catch (IOException exc) {
      Loggers.getLogger().error("Error copying to file {}", file, exc);
      return Result.error("IO error while copying file");
    }

    return Result.success(file);
  }

  public void deleteIcon(String id) {
    Path path = getIconPath(id);

    if (path == null) {
      return;
    }

    PathUtil.safeDelete(path, false, false);
  }
}
