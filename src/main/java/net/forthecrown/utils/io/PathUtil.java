package net.forthecrown.utils.io;

import com.google.errorprone.annotations.FormatString;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import net.forthecrown.core.Crown;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PathUtil {
    private PathUtil() {}

    public static Path getPluginDirectory() {
        return Crown.plugin().getDataFolder().toPath();
    }

    public static Path pluginPath(String first, String... others) {
        return getPluginDirectory().resolve(Paths.get(first, others));
    }

    /**
     * Macro for {@link #pluginPath(String, String...)} and
     * {@link #ensureDirectoryExists(Path)} to ensure a directory
     * within the plugin's data folder exists.
     * <p>
     * Calls {@link #ensureDirectoryExists(Path)} with the given
     * path name(s) and then calls {@link Either#orThrow()} on
     * the result, this will either return the input path or
     * throw an exception
     * @param first The first part of the path
     * @param others The rest
     *
     * @return The created plugin
     *
     * @throws RuntimeException If the path couldn't be created, or if
     *                          the path was not a folder and couldn't
     *                          be deleted.
     */
    public static Path getPluginDirectory(String first, String... others) throws RuntimeException {
        return ensureDirectoryExists(pluginPath(first, others)).orThrow();
    }

    /**
     * Ensure the given directory exists.
     * <p>
     * If the given path is not a directory it will be
     * deleted and this will then attempt to create
     * a directory named by the given abstract pathname.
     *
     * @param path The path to ensure exists
     * @return The result, if any step of the process failed, this is
     *         returned with the thrown {@link IOException}, if this
     *         method did not fail, the result will contain the created
     *         directory path
     */
    public static Either<Path, IOException> ensureDirectoryExists(Path path) {
        try {
            if (Files.exists(path) && !Files.isDirectory(path)) {
                Files.delete(path);
            }

            return Either.left(Files.createDirectories(path));
        } catch (IOException exc) {
            return Either.right(exc);
        }
    }

    /**
     * Safely iterates through the given directory path.
     * <p>
     * If a {@link IOException} is thrown during the directory stream
     * creation, then this will return a failed resuslt, otherwise
     * the return value will depend on the <code>tolerateErrors</code>
     * value. If that value is true, then this will return a successful
     * result no matter what, otherwise the returned result will
     * contain a partial result.
     * <p>
     * This will also return a non-partial failure result if the given
     * path is not a directory
     *
     * @param dir The directory to iterate through
     * @param deep True, to also iterate through subdirectories, if
     *             false, subdirectories are ignored
     *
     * @param tolerateErrors If set to true, then any errors thrown
     *                       will only be logged and the iteration
     *                       will continue, if false, any errors
     *                       thrown will stop the iteration
     *
     * @param fileConsumer The consumer to apply to all non-directory
     *                     paths in the given directory
     *
     * @return A data result containing the amount of files iterated
     *         through successfully, if this fails, the result's partial
     *         result will contain the amount of files iterated through
     */
    public static DataResult<Integer> iterateDirectory(Path dir,
                                                       boolean deep,
                                                       boolean tolerateErrors,
                                                       IOConsumer<Path> fileConsumer
    ) {
        if (!Files.isDirectory(dir)) {
            return errorResult("Path '%s' is not a directory", dir);
        }

        try (var stream = Files.newDirectoryStream(dir)) {
            int deleted = 0;

            for (var p: stream) {
                if (Files.isDirectory(p)) {
                    if (!deep) {
                        continue;
                    }

                    var result = iterateDirectory(p, true, tolerateErrors, fileConsumer);
                    var either = result.get();

                    if (either.left().isPresent()) {
                        deleted += either.left().get();
                    } else if (!tolerateErrors) {
                        final int finalDeleted = deleted;
                        return result.map(integer -> integer + finalDeleted);
                    }
                }

                try {
                    fileConsumer.accept(p);
                    deleted++;
                } catch (IOException exc) {
                    if (!tolerateErrors) {
                        return partialResult(deleted,
                                "Couldn't perform operation on file '%s': '%s'",
                                p, exc.getMessage()
                        );
                    }
                }
            }

            return DataResult.success(deleted);
        } catch (IOException e) {
            return errorResult("Couldn't iterate through directory '%s': '%s'", e);
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
     * will recursively iterate through every subdirectory of the
     * given path and attempt to delete those as well.
     * <p>
     * If the deletion process encounters an error in deleting files
     * then this will either return a partial result with an appropriate
     * error message or continue attempting to delete files, depending
     * on the value of <code>tolerateErrors</code>.
     * <p>
     * Note: This method will only catch {@link IOException} instances
     * <p>
     * If the given files does not exist, this returns a successful
     * result with 0 deleted.
     *
     * @param path The path to delete
     *
     * @param tolerateErrors Determines whether the deletion process
     *                       should halt the moment an error is found
     *                       or continue until an attempt has been made
     *                       to delete all sub paths and subdirectories
     *
     * @param recursive True, to also delete all subdirectories of the
     *                  given path, false otherwise
     *
     * @return Deletion result, will be partial if unsuccessful, otherwise
     *         will contain the amount of files deleted
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
                for (var p: stream) {
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
                return partialResult(deleted,
                        "Couldn't recursively delete directory '%s': '%s'", path, e.getMessage()
                );
            }
        }

        try {
            Files.delete(path);
            deleted++;

            return DataResult.success(deleted);
        } catch (IOException e) {
            return partialResult(deleted,
                    "Couldn't delete file: '%s': '%s'",
                    path, e.getMessage()
            );
        }
    }

    public static <T> DataResult<T> errorResult(String msgFormat, Object... args) {
        return DataResult.error(String.format(msgFormat, args));
    }

    public static <T> DataResult<T> partialResult(T partial, @FormatString String msgFormat, Object... args) {
        return DataResult.error(
                String.format(msgFormat, args),
                partial
        );
    }
}