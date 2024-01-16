package net.forthecrown.utils.io;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.utils.PluginUtil;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

public class PluginJar {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final Field javaPlugin_file;

  private static final Map<String, FileSystem> pluginJars = new Object2ObjectOpenHashMap<>();

  static {
    try {
      Class<JavaPlugin> pluginClass = JavaPlugin.class;
      Field f = pluginClass.getDeclaredField("file");
      f.setAccessible(true);
      javaPlugin_file = f;
    } catch (ReflectiveOperationException exc) {
      throw new RuntimeException(exc);
    }
  }

  /* ---------------------- JAR FILE SYSTEM ACCESS ------------------------ */

  private static FileSystem getResourceFileSystem(JavaPlugin plugin) {
    String name = plugin.getName();
    FileSystem foundSystem = pluginJars.get(name);

    if (foundSystem != null) {
      return foundSystem;
    }

    Path jarPath = reflectivelyGetPluginJar(plugin).toPath();

    try {
      FileSystem system = FileSystems.newFileSystem(jarPath);
      pluginJars.put(name, system);
      return system;
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
  }

  /**
   * Gets a path to a file inside the plugin jar
   */
  public static Path resourcePath(String s, String... others) {
    JavaPlugin caller = PluginUtil.getCallingPlugin();
    return resourcePath(caller, s, others);
  }

  public static Path resourcePath(JavaPlugin plugin, String s, String... others) {
    return getResourceFileSystem(plugin).getPath(s, others);
  }

  public static void saveResources(JavaPlugin plugin, String sourceDir) {
    Path dest = plugin.getDataFolder().toPath().resolve(sourceDir);
    _saveResources(plugin, sourceDir, dest);
  }

  public static void saveResources(String sourceDir) {
    JavaPlugin caller = PluginUtil.getCallingPlugin();
    Path dest = caller.getDataFolder().toPath().resolve(sourceDir);
    _saveResources(caller, sourceDir, dest);
  }

  public static void saveResources(JavaPlugin plugin, String sourceDir, Path dest) {
    _saveResources(plugin, sourceDir, dest);
  }

  public static void saveResources(String sourceDir, Path dest) {
    JavaPlugin caller = PluginUtil.getCallingPlugin();
    _saveResources(caller, sourceDir, dest);
  }

  private static void _saveResources(JavaPlugin plugin, String sourceDir, Path dest) {
    Path jarDir = resourcePath(plugin, sourceDir);

    if (!Files.exists(jarDir)) {
      LOGGER.warn("Cannot save plugin path {}! Doesn't exist", jarDir);
      return;
    }

    DirectoryCopyWalker walker = new DirectoryCopyWalker(jarDir, dest);

    try {
      Files.walkFileTree(jarDir, walker);
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
  }

  private static File reflectivelyGetPluginJar(JavaPlugin plugin) {
    try {
      File f = (File) javaPlugin_file.get(plugin);
      return f;
    } catch (ReflectiveOperationException exc) {
      throw new RuntimeException(exc);
    }
  }

  @Getter
  @RequiredArgsConstructor
  private static class DirectoryCopyWalker extends SimpleFileVisitor<Path> {

    private final Path source;
    private final Path dest;

    private Path resolveRelativeAsString(final Path directory) {
      return dest.resolve(source.relativize(directory).toString());
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir,
                                             BasicFileAttributes attrs
    ) throws IOException {
      var destDir = resolveRelativeAsString(dir);
      if (Files.notExists(destDir)) {
        Files.createDirectories(destDir);
      }

      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
        throws IOException
    {
      var destDir = resolveRelativeAsString(file);

      if (!maySaveResource(attrs, destDir)) {
        return FileVisitResult.CONTINUE;
      }

      if (Files.exists(destDir)) {
        LOGGER.warn("Overwriting {} with jar resource {}",
            destDir, file
        );
      }

      Files.copy(
          file, destDir,
          StandardCopyOption.REPLACE_EXISTING
      );
      return FileVisitResult.CONTINUE;
    }

    private boolean maySaveResource(BasicFileAttributes sourceAttr, Path dest) throws IOException {
      return !Files.exists(dest);

      // This shit does not work with so many things modifying the file attributes
      // Git ignores file attributes alltogether, so that just breaks the intended
      // functionality of all of this
      /*if (!Files.exists(dest)) {
        return true;
      }

      if ((flags & ALLOW_OVERWRITE) == 0) {
        return false;
      }

      if ((flags & OVERWRITE_IF_NEWER) == 0) {
        return true;
      }

      var destAttr = PathUtils.readBasicFileAttributes(dest);

      var sLastModified = sourceAttr.lastModifiedTime().toInstant();
      var dLastModified = destAttr.lastModifiedTime().toInstant();

      return sLastModified.isAfter(dLastModified);*/
    }
  }
}