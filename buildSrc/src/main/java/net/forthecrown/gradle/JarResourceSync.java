package net.forthecrown.gradle;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Objects;

public class JarResourceSync extends SimpleFileVisitor<Path> {
  private final Path resourcesSource;
  private final FileSystem jarFileSystem;

  public JarResourceSync(Path resourcesSource, FileSystem jarFileSystem) {
    this.resourcesSource = Objects.requireNonNull(resourcesSource);
    this.jarFileSystem = Objects.requireNonNull(jarFileSystem);
  }

  public static void sync(Path resourceDirectory, Path jarFile)
      throws IOException, URISyntaxException
  {
    URI uri = new URI("jar", jarFile.toUri().toString(), null);

    FileSystem fs;

    try {
      fs = FileSystems.getFileSystem(uri);
    } catch (FileSystemNotFoundException exc) {
      fs = FileSystems.newFileSystem(uri, Collections.singletonMap("create", "true"));
    }

    var walker = new JarResourceSync(resourceDirectory, fs);
    Files.walkFileTree(resourceDirectory, walker);

    fs.close();
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
      throws IOException
  {
    var relative = resourcesSource.relativize(file).toString();
    var dest = jarFileSystem.getPath(relative);

    if (!Files.exists(dest)) {
      return FileVisitResult.CONTINUE;
    }

    var time = attrs.lastModifiedTime();
    Files.setLastModifiedTime(dest, time);

    return FileVisitResult.CONTINUE;
  }
}