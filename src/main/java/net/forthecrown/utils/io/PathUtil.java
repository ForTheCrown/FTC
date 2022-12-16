package net.forthecrown.utils.io;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class PathUtil {
    private PathUtil() {}

    private static final Logger LOGGER = FTC.getLogger();

    /** Gets the plugin's data folder */
    public static Path getPluginDirectory() {
        return FTC.getPlugin().getDataFolder().toPath();
    }

    /** Creates a path inside the plugin's data folder */
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
    public static Path getPluginDirectory(String first, String... others)
            throws RuntimeException
    {
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
            return Results.errorResult("Path '%s' is not a directory", dir);
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
                        return Results.partialResult(deleted,
                                "Couldn't perform operation on file '%s': '%s'",
                                p, exc.getMessage()
                        );
                    }
                }
            }

            return DataResult.success(deleted);
        } catch (IOException e) {
            return Results.errorResult("Couldn't iterate through directory '%s': '%s'", e);
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
                return Results.partialResult(deleted,
                        "Couldn't recursively delete directory '%s': '%s'", path, e.getMessage()
                );
            }
        }

        try {
            Files.delete(path);
            deleted++;

            return DataResult.success(deleted);
        } catch (IOException e) {
            return Results.partialResult(deleted,
                    "Couldn't delete file: '%s': '%s'",
                    path, e.getMessage()
            );
        }
    }

    /**
     * Archives the given source directory to the destination as a
     * ZIP file.
     *
     * @param source The source directory to zip up
     * @param dest The destination path of the ZIP file
     *
     * @throws IOException If an IO error occurs
     */
    public static void archive(Path source, Path dest) throws IOException {
        var parent = dest.getParent();

        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }

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

    public static DataResult<List<String>> findAllFiles(Path directory,
                                                        boolean includeFileFormats
    ) {
        FileFinderWalker walker = new FileFinderWalker(
                includeFileFormats, directory
        );

        try {
            Files.walkFileTree(directory, walker);
            return DataResult.success(walker.results);
        } catch (IOException exc) {
            return Results.partialResult(walker.results,
                    "Error walking tree! %s",
                    exc.getMessage()
            );
        }
    }

    /* ---------------------------- SUB CLASSES ----------------------------- */

    @RequiredArgsConstructor
    private static class FileFinderWalker extends SimpleFileVisitor<Path> {
        private final boolean includeFormats;
        private final Path root;

        private final List<String> results = new ObjectArrayList<>();

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            results.add(fileName(root.relativize(file).toString()));
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