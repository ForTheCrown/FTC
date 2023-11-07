package net.forthecrown.utils.io;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.utils.PluginUtil;
import org.apache.commons.io.file.PathUtils;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;

public final class PathUtil {
  private PathUtil() {}

  private static final Logger LOGGER = Loggers.getLogger();

  public static final Pattern UUID_PATTERN
      = Pattern.compile(
          "[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}",
          CASE_INSENSITIVE
        );

  /**
   * Gets the calling plugin's data folder path
   * @return Calling plugin's data folder
   */
  public static Path pluginPath() {
    Plugin caller = PluginUtil.getCallingPlugin();
    return caller.getDataFolder().toPath();
  }

  /**
   * Gets a {@link Path} object inside the plugin that called this function
   * <p>
   * Uses {@link PluginUtil#getCallingPlugin()} to get the plugin that invoked this function
   *
   * @param first First path element
   * @param others Other path elements
   * @return Path
   */
  public static Path pluginPath(String first, String... others) {
    Plugin caller = PluginUtil.getCallingPlugin();
    return pluginPath(caller, first, others);
  }

  public static Path pluginPath(Plugin plugin, String first, String... others) {
    Path path = Path.of(first, others);
    Path directory = plugin.getDataFolder().toPath();
    return directory.resolve(path);
  }

  /**
   * Ensure the given directory exists.
   * <p>
   * If the given path is not a directory it will be deleted and this will then attempt to create a
   * directory named by the given abstract pathname.
   *
   * @param path The path to ensure exists
   * @return The result, if any step of the process failed, this is returned with the thrown
   * {@link IOException}, if this method did not fail, the result will contain the created directory
   * path
   */
  public static Path ensureDirectoryExists(Path path) {
    try {
      if (Files.exists(path) && !Files.isDirectory(path)) {
        Files.delete(path);
      }

      return Files.createDirectories(path);
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
  }

  /**
   * Safely iterates through the given directory path.
   * <p>
   * If a {@link IOException} is thrown during the directory stream creation, then this will return
   * a failed resuslt, otherwise the return value will depend on the <code>tolerateErrors</code>
   * value. If that value is true, then this will return a successful result no matter what,
   * otherwise the returned result will contain a partial result.
   * <p>
   * This will also return a non-partial failure result if the given path is not a directory
   *
   * @param dir            The directory to iterate through
   * @param deep           True, to also iterate through subdirectories, if false, subdirectories
   *                       are ignored
   * @param tolerateErrors If set to true, then any errors thrown will only be logged and the
   *                       iteration will continue, if false, any errors thrown will stop the
   *                       iteration
   * @param fileConsumer   The consumer to apply to all non-directory paths in the given directory
   * @return A data result containing the amount of files iterated through successfully, if this
   * fails, the result's partial result will contain the amount of files iterated through
   */
  public static DataResult<Integer> iterateDirectory(
      Path dir,
      boolean deep,
      boolean tolerateErrors,
      IOConsumer<Path> fileConsumer
  ) {
    if (!Files.isDirectory(dir)) {
      return Results.error("Path '%s' is not a directory", dir);
    }

    DataResult<Integer> result = DataResult.success(0);

    try (var stream = Files.newDirectoryStream(dir)) {
      for (var p : stream) {
        if (Files.isDirectory(p) && deep) {
          var subResult = iterateDirectory(p, true, tolerateErrors, fileConsumer);
          result = result.apply2(Integer::sum, subResult);

          if (subResult.error().isPresent()) {
            LOGGER.error(subResult.error().get().message());

            if (!tolerateErrors) {
              return result;
            }
          }

          continue;
        }

        try {
          fileConsumer.accept(p);
          result = result.map(integer -> integer + 1);
        } catch (IOException exc) {
          var err = Results.error(
              "Couldn't perform operation on file '%s': '%s'",
              p, exc.getMessage()
          );

          result = result.apply2((integer, o) -> integer, err);

          if (!tolerateErrors) {
            return result;
          } else {
            LOGGER.error("Error iterating over path {}", p, exc);
          }
        }
      }

      return result;
    } catch (IOException e) {
      return Results.error("Couldn't iterate through directory '%s': '%s'", e);
    }
  }

  public static DataResult<Integer> safeDelete(Path path) {
    return safeDelete(path, true, true);
  }

  public static DataResult<Integer> safeDelete(Path path, boolean tolerateErrors) {
    return safeDelete(path, tolerateErrors, true);
  }

  /**
   * Safely and recursively deletes the given path. If the
   * <code>recursive</code> parameter is set to true, then this
   * will recursively iterate through every subdirectory of the given path and attempt to delete
   * those as well.
   * <p>
   * If the deletion process encounters an error in deleting files then this will either return a
   * partial result with an appropriate error message or continue attempting to delete files,
   * depending on the value of <code>tolerateErrors</code>.
   * <p>
   * Note: This method will only catch {@link IOException} instances
   * <p>
   * If the given files does not exist, this returns a successful result with 0 deleted.
   *
   * @param path           The path to delete
   * @param tolerateErrors Determines whether the deletion process should halt the moment an error
   *                       is found or continue until an attempt has been made to delete all sub
   *                       paths and subdirectories
   * @param recursive      True, to also delete all subdirectories of the given path, false
   *                       otherwise
   * @return Deletion result, will be partial if unsuccessful, otherwise will contain the amount of
   * files deleted
   */
  public static DataResult<Integer> safeDelete(Path path,
                                               boolean tolerateErrors,
                                               boolean recursive
  ) {
    int deleted = 0;

    if (!Files.exists(path)) {
      return DataResult.success(deleted);
    }

    // attempt to delete all subdirectories if
    // this is a directory, and we're allowed to
    if (Files.isDirectory(path) && recursive) {
      try (var stream = Files.newDirectoryStream(path)) {
        for (var p : stream) {
          var result = safeDelete(p, tolerateErrors, true);
          var either = result.get();

          if (result.error().isEmpty()) {
            deleted += either.left().orElse(0);
          } else if (!tolerateErrors) {
            final int finalDeleted = deleted;
            return result.map(integer -> integer + finalDeleted);
          }
        }
      } catch (IOException e) {
        // Don't test for error toleration here because
        // if a directory isn't empty when we try to delete it,
        // it'll fail anyway
        return Results.partial(deleted,
            "Couldn't recursively delete directory '%s': '%s'", path, e.getMessage()
        );
      }
    }

    try {
      Files.delete(path);
      deleted++;

      return DataResult.success(deleted);
    } catch (IOException e) {
      return Results.partial(deleted,
          "Couldn't delete file: '%s': '%s'",
          path, e.getMessage()
      );
    }
  }

  /**
   * Archives the given source directory to the destination as a ZIP file.
   *
   * @param source The source directory to zip up
   * @param dest   The destination path of the ZIP file
   * @throws IOException If an IO error occurs
   */
  public static void archive(Path source, Path dest) throws IOException {
    ensureParentExists(dest);

    try (var stream = new ZipOutputStream(Files.newOutputStream(dest))) {
      try (var pStream = Files.walk(source)) {
        pStream
            .filter(path -> !Files.isDirectory(path))
            .forEach(path -> {
              ZipEntry entry = new ZipEntry(
                  source.relativize(path).toString()
              );

              try {
                stream.putNextEntry(entry);
                Files.copy(path, stream);
                stream.closeEntry();
              } catch (IOException exc) {
                exc.printStackTrace();
              }
            });
      }
    }
  }

  public static void move(Path source, Path destination) throws IOException {
    if (!Files.exists(source)) {
      return;
    }

    safeDelete(destination);
    copyFiles(source, destination);
    safeDelete(source);
  }

  public static void copy(Path source, Path destination) throws IOException {
    if (!Files.exists(source)) {
      return;
    }

    safeDelete(destination);
    copyFiles(source, destination);
  }

  private static void copyFiles(Path source, Path destination) throws IOException {
    ensureParentExists(destination);

    if (Files.isDirectory(source)) {
      PathUtils.copyDirectory(source, destination);
    } else {
      Files.copy(source, destination);
    }
  }

  public static List<String> findAllFiles(Path directory, boolean includeFileFormats) {
    return findAllFiles(directory, includeFileFormats, null);
  }

  public static List<String> findAllFiles(
      Path directory,
      boolean includeFileFormats,
      Predicate<Path> filter
  ) {
    FileFinderWalker walker = new FileFinderWalker(
        includeFileFormats, directory, filter
    );

    try {
      Files.walkFileTree(directory, walker);
      return walker.results;
    } catch (IOException exc) {
      Loggers.getLogger().error("Error walking file tree, dir={}", directory, exc);
      return ObjectLists.emptyList();
    }
  }

  public static boolean isFilenameUUID(Path path) {
    return UUID_PATTERN.matcher(path.getFileName().toString()).find();
  }

  public static DataResult<UUID> getFilenameUUID(Path path) {
    var matcher = UUID_PATTERN.matcher(path.getFileName().toString());
    var arr = matcher.results()
        .map(result -> UUID.fromString(result.group()))
        .toArray(UUID[]::new);

    if (arr.length < 1) {
      return Results.error("File %s is not a UUID file", path);
    }

    if (arr.length > 1) {
      return Results.partial(
          arr[0],
          "Path %s contained more than 1 UUID",
          path
      );
    }

    return DataResult.success(arr[0]);
  }

  public static void ensureParentExists(Path file) throws IOException {
    if (Files.exists(file)) {
      return;
    }

    var parent = file.getParent();

    // File exists in top level directory with no parent folder
    if (parent != null) {
      if (Files.exists(parent) && !Files.isDirectory(parent)) {
        Files.delete(parent);
      }

      Files.createDirectories(parent);
    }
  }

  /* ---------------------------- SUB CLASSES ----------------------------- */

  @RequiredArgsConstructor
  private static class FileFinderWalker extends SimpleFileVisitor<Path> {

    private final boolean includeFormats;
    private final Path root;

    private final Predicate<Path> filter;

    private final List<String> results = new ObjectArrayList<>();

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      if (filter != null && !filter.test(file)) {
        return FileVisitResult.CONTINUE;
      }

      String s = fileName(root.relativize(file).toString());

      // I hate Windows' directory separators
      results.add(s.replaceAll("\\\\", "/"));

      return FileVisitResult.CONTINUE;
    }

    private String fileName(String path) {
      if (path.startsWith("/")) {
        path = path.substring(1);
      }

      if (includeFormats) {
        return path;
      }

      int dotIndex = path.indexOf('.');

      if (dotIndex == -1) {
        return path;
      }

      return path.substring(0, dotIndex);
    }
  }
}