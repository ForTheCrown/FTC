package net.forthecrown.datafix;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.forthecrown.utils.io.IOConsumer;
import net.forthecrown.utils.io.PathUtil;

public abstract class DataUpdater {

  private final ImmutableMap<String, String> renames;
  protected UpdaterLogger logger;

  public DataUpdater() {
    var builder = ImmutableMap.<String, String>builder();
    createRenames(builder);

    this.renames = builder.build();
  }

  final void bind(DataUpdaters updaters) {
    logger = UpdaterLogger.create(updaters.getPlugin());
  }

  protected void createRenames(ImmutableMap.Builder<String, String> builder) {
  }

  /**
   * Runs this updater
   *
   * @return The updater's execution result
   */
  public final boolean runUpdater() {
    if (!shouldRun()) {
      return false;
    }

    if (logger == null) {
      return false;
    }

    if (logger.getOutput() == null) {
      logger.initFilePrinter();
    }

    logger.info("Starting updater: {}", getClass().getSimpleName());

    try {
      if (update()) {
        logger.info("{} completed successfully", getClass().getSimpleName());
        return true;
      }

      logger.warn("{} completed unsuccessfully", getClass().getSimpleName());
      return false;
    } catch (Throwable t) {
      logger.error("Error running: {}", getClass().getSimpleName(), t);
      return false;
    }
  }

  protected abstract boolean update() throws Throwable;

  protected boolean shouldRun() {
    return true;
  }

  // --- UTILITIES ---

  protected <T> Either<T, String> rename(DynamicOps<T> ops, T value, final boolean deep) {
    var mapResult = ops.getMapValues(value);
    var listResult = ops.getStream(value);

    // If element is map
    if (mapResult.result().isPresent()) {
      return Either.left(renameMap(ops, mapResult.result().get(), deep));
    }

    // If element is list
    if (listResult.result().isPresent()) {
      return Either.left(renameList(ops, listResult.result().get(), deep));
    }

    return Either.right("Given value is not a list/map type object");
  }

  private <T> T renameList(DynamicOps<T> ops, Stream<T> stream, boolean deep) {
    return ops.createList(
        stream.map(t -> {
          var either = rename(ops, t, deep);

          if (either.left().isPresent()) {
            return either.left().get();
          }

          return t;
        })
    );
  }

  private <T> T renameMap(DynamicOps<T> ops, Stream<Pair<T, T>> stream, boolean deep) {
    return ops.createMap(
        stream.map(pair -> renamePair(pair, ops, deep))
    );
  }

  private <T> Pair<T, T> renamePair(Pair<T, T> entry, DynamicOps<T> ops, boolean recursive) {
    DataResult<String> firstString = ops.getStringValue(entry.getFirst());

    if (firstString.get().left().isEmpty()) {
      return Pair.of(entry.getFirst(), entry.getFirst());
    }

    T entryValue = entry.getSecond();

    if (recursive) {
      Either<T, String> either = rename(ops, entryValue, true);

      // If entry value was both a map object and
      // was successfully renamed
      if (either.left().isPresent()) {
        entryValue = either.left().get();
      }
    }

    String key = firstString.get().left().get();

    return Pair.of(
        ops.createString(renames.getOrDefault(key, key)),
        entryValue
    );
  }

  // --- STATIC UTILITIES ---

  /**
   * Moves the given source file to the given destination file.
   * <p>
   * If any errors are thrown during file movement, then this catches it and logs it to the
   * {@link #logger}
   *
   * @param source The source path to move
   * @param dest   The destination file
   * @return True, if the source existed, and it was successfully moved
   */
  public final boolean moveSafe(Path source, Path dest) {
    if (!Files.exists(source)) {
      return false;
    }

    try {
      PathUtil.move(source, dest);
      return true;
    } catch (IOException exc) {
      logger.error("Error moving file '{}' to '{}'", source, dest, exc);
      return false;
    }
  }

  public final boolean copySafe(Path source, Path dest) {
    if (!Files.exists(source)) {
      return false;
    }

    try {
      PathUtil.copy(source, dest);
      return true;
    } catch (IOException exc) {
      logger.error("Error copying file '{}' to '{}'", source, dest, exc);
      return false;
    }
  }

  public final boolean safeDelete(Path path) {
    logger.info("Deleting '{}'", path);

    return PathUtil.safeDelete(path)
        .resultOrPartial(string -> {
          logger.error("Error deleting '{}': {}", path, string);
        })
        .isPresent();
  }

  /**
   * Delegate method for {@link PathUtil#iterateDirectory(Path, boolean, boolean, IOConsumer)}
   *
   * @return True, if the delegate method successfully iterated through all files in the given
   * directory, false otherwise
   * @see PathUtil#iterateDirectory(Path, boolean, boolean, IOConsumer)
   */
  public boolean iterateDirectory(
      Path dir,
      boolean deep,
      boolean tolerateErrors,
      IOConsumer<Path> fileConsumer
  ) {
    var result = PathUtil.iterateDirectory(dir, deep, tolerateErrors, fileConsumer);
    return result.resultOrPartial(logger::error).orElse(0) > 0;
  }

}