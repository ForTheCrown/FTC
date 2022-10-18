package net.forthecrown.datafix;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import net.forthecrown.utils.io.IOConsumer;
import net.forthecrown.utils.io.PathUtil;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public abstract class DataUpdater {
    protected static final UpdaterLogger LOGGER = UpdaterLogger.create();

    private final ImmutableMap<String, String> renames;

    public DataUpdater() {
        var builder = ImmutableMap.<String, String>builder();
        createRenames(builder);

        this.renames = builder.build();
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

        LOGGER.info("Starting updater: {}", getClass().getSimpleName());

        try {
            if (update()) {
                LOGGER.info("{} completed successfully", getClass().getSimpleName());
                return true;
            }

            LOGGER.warn("{} completed unsuccessfully", getClass().getSimpleName());
            return false;
        } catch (Throwable t) {
            LOGGER.error("Error running: {}", getClass().getSimpleName(), t);
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
        var firstString = ops.getStringValue(entry.getFirst());

        if (firstString.get().left().isEmpty()) {
            return Pair.of(entry.getFirst(), entry.getFirst());
        }

        var entryValue = entry.getSecond();

        if (recursive) {
            var either = rename(ops, entryValue, true);

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
     * Moves the given source file to the given
     * destination file.
     * <p>
     * If any errors are thrown during file movement,
     * then this catches it and logs it to the {@link #LOGGER}
     *
     * @param source The source path to move
     * @param dest The destination file
     * @return True, if the source existed, and
     *         it was successfully moved
     */
    public static boolean moveSafe(Path source, Path dest) {
        if (!Files.exists(source)) {
            return false;
        }

        try {
            Files.move(
                    source, dest,
                    StandardCopyOption.REPLACE_EXISTING
            );
            return true;
        } catch (IOException exc) {
            LOGGER.error("Error moving file '{}' to '{}'",
                    source, dest, exc
            );
            return false;
        }
    }

    /**
     * Delegate method for {@link PathUtil#iterateDirectory(Path, boolean, boolean, IOConsumer)}
     * @see PathUtil#iterateDirectory(Path, boolean, boolean, IOConsumer)
     *
     * @return True, if the delegate method successfully iterated through all files
     *         in the given directory, false otherwise
     */
    public static boolean iterateDirectory(Path dir,
                                    boolean deep,
                                    boolean tolerateErrors,
                                    IOConsumer<Path> fileConsumer
    ) {
        var result = PathUtil.iterateDirectory(dir, deep, tolerateErrors, fileConsumer);

        return result.resultOrPartial(LOGGER::error)
                .orElse(0) > 0;
    }

    /**
     * Performs the given input as a command executed by the
     * server's console
     * @param format The command format
     * @param args Any arguments to give to the format
     */
    @FormatMethod
    protected static void consoleCommand(@FormatString String format, Object... args) {
        Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                format.formatted(args)
        );
    }
}