package net.forthecrown.utils.io;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class FtcJar {
    private static final Logger LOGGER = FTC.getLogger();

    /**
     * Flag for resource save operation which allows jar resources to overwrite
     * existing files if, and only if, the resource on the jar was modified
     * after the file being overwritten.
     * <p>
     * Note that this flag still requires {@link #ALLOW_OVERWRITE} to be set in
     * order for any files to be overwritten.
     */
    public static final int OVERWRITE_IF_NEWER = 0x2;

    /**
     * Flag which tells the resource save operation to overwrite existing files
     */
    public static final int ALLOW_OVERWRITE = 0x1;

    /** The ZIP file system of the plugin jar */
    public static final FileSystem JAR_FILE_SYSTEM;

    static {
        try {
            URI jarUri = PathUtil.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();

            URI uri = new URI("jar", jarUri.toString(), null);

            Map<String, String> env = new HashMap<>();
            env.put("create", "true");

            try {
                JAR_FILE_SYSTEM = FileSystems.newFileSystem(
                        uri,
                        env,
                        PathUtil.class.getClassLoader()
                );

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (URISyntaxException exc) {
            throw new RuntimeException(exc);
        }
    }

    /* ---------------------- JAR FILE SYSTEM ACCESS ------------------------ */

    /** Gets a path to a file inside the plugin jar */
    public static Path resourcePath(String s, String... others) {
        return JAR_FILE_SYSTEM.getPath(s, others);
    }

    /**
     * Saves a file/directory from the plugin jar resources to the FTC
     * data folder
     *
     * @param sourceDir The path to the directory/file inside the jar,
     *                  will also act as the path the file/directory is
     *                  saved to.
     *
     * @param flags Flags to use for determining whether a file should be
     *              overwritten or not, Set to 0 to never override existing
     *              files
     *
     * @throws IOException If an IO error occurred
     * @see #saveResources(String, Path, int)
     */
    public static void saveResources(String sourceDir, int flags)
            throws IOException
    {
        saveResources(
                sourceDir,
                PathUtil.pluginPath(sourceDir),
                flags
        );
    }

    public static void saveResources(String sourceDir) throws IOException {
        saveResources(sourceDir, 0);
    }

    public static void saveResources(String sourceDir, Path dest)
            throws IOException
    {
        saveResources(sourceDir, dest, 0);
    }

    /**
     * Saves a file/directory from the plugin jar resources to the provided
     * path.
     * <p>
     * If the provided path is a file, it will simply write its contents to
     * provided path.
     * @param sourceDir The path to the source file/directory
     * @param dest The destination path to save to
     * @param flags Flags to use for determining whether a file should be
     *              overwritten or not, Set to 0 to never override existing
     *              files
     *
     * @throws IOException If an IO error occurs
     * @see #ALLOW_OVERWRITE
     * @see #OVERWRITE_IF_NEWER
     */
    public static void saveResources(String sourceDir,
                                     Path dest,
                                     int flags
    ) throws IOException {
        Path jarDir = resourcePath(sourceDir);

        if (!Files.exists(jarDir)) {
            LOGGER.warn("Cannot save plugin path {}! Doesn't exist", jarDir);
            return;
        }

        DirectoryCopyWalker walker = new DirectoryCopyWalker(
                jarDir, dest, flags
        );

        Files.walkFileTree(jarDir, walker);
    }

    @Getter
    @RequiredArgsConstructor
    private static class DirectoryCopyWalker extends SimpleFileVisitor<Path> {
        private final Path source;
        private final Path dest;
        private final int flags;

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

        private boolean maySaveResource(BasicFileAttributes sourceAttr,
                                        Path dest
        ) throws IOException {
            if (!Files.exists(dest)) {
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

            LOGGER.debug("sourceLastModified={}", sLastModified);
            LOGGER.debug("  destLastModified={}", dLastModified);

            LOGGER.debug("dif={}",
                    sLastModified.toEpochMilli() - dLastModified.toEpochMilli()
            );

            return sLastModified.isAfter(dLastModified);
        }
    }
}