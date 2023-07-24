package net.forthecrown.utils.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import org.apache.logging.log4j.Logger;
import org.tomlj.Toml;
import org.tomlj.TomlTable;

public final class SerializationHelper {
  private SerializationHelper() {}

  private static final Logger LOGGER = Loggers.getLogger();

  public static final IoReader<CompoundTag>
      TAG_READER = file -> BinaryTags.readCompressed(Files.newInputStream(file));

  public static final IoReader<JsonObject>
      JSON_READER = JsonUtils::readFileObject;

  public static void ensureParentExists(Path file) throws IOException {
    if (Files.exists(file)) {
      return;
    }

    var parent = file.getParent();

    // File exists in top level directory with no parent folder
    if (parent != null) {
      var either = PathUtil.ensureDirectoryExists(parent);

      if (either.right().isPresent()) {
        throw either.right().get();
      }
    }
  }

  public static <T> DataResult<T> readFileObject(Path file, IoReader<T> reader) {
    if (!Files.exists(file)) {
      return Results.error("File '%s' doesn't exist", file);
    }

    try {
      return Results.success(reader.apply(file));
    } catch (IOException e) {
      LOGGER.error("Error writing file: '" + file + "'", e);
      return Results.error(e.getMessage());
    }
  }

  public static <T> boolean readFile(Path file,
                                     IoReader<T> reader,
                                     Consumer<T> loadCallback
  ) {
    Optional<T> result = readFileObject(file, reader)
        .resultOrPartial(s -> {
          if (s.contains("doesn't exist") && s.startsWith("File '")) {
            return;
          }

          LOGGER.error(s);
        });

    if (result.isEmpty()) {
      return false;
    }

    loadCallback.accept(result.get());
    return true;
  }

  public static DataResult<CompoundTag> readTag(Path path) {
    return readFileObject(path, TAG_READER);
  }

  public static DataResult<JsonObject> readJson(Path path) {
    return readFileObject(path, JSON_READER);
  }

  public static DataResult<JsonObject> readTomlAsJson(Path path) {
    return readFileObject(path, Toml::parse).flatMap(t -> {
      if (!t.errors().isEmpty()) {
        t.errors().forEach(LOGGER::error);
        return Results.error("TOML read errors");
      }

      return Results.success(TomlUtil.toJson(t));
    });
  }

  public static boolean readTagFile(Path file, Consumer<CompoundTag> loadCallback) {
    return readFile(file, TAG_READER, loadCallback);
  }

  public static boolean readTomlFile(Path file, Consumer<TomlTable> consumer) {
    return readFile(file, Toml::parse, result -> {
      if (!result.errors().isEmpty()) {
        result.errors().forEach(LOGGER::error);
        return;
      }

      consumer.accept(result);
    });
  }

  public static boolean readTomlAsJson(Path file, Consumer<JsonWrapper> callback) {
    return readTomlFile(file, table -> {
      JsonObject obj = TomlUtil.toJson(table);
      JsonWrapper json = JsonWrapper.wrap(obj);
      callback.accept(json);
    });
  }

  public static boolean readJsonFile(Path file, Consumer<JsonWrapper> loadCallback) {
    return readFile(file, JSON_READER, object -> loadCallback.accept(JsonWrapper.wrap(object)));
  }

  public static void writeFile(Path file, IoWriter writer) {
    try {
      ensureParentExists(file);
      writer.apply(file);
    } catch (IOException e) {
      LOGGER.error("Error writing file: '" + file + "'", e);
    }
  }

  public static void writeTag(Path f, CompoundTag tag) {
    writeFile(f, file -> {
      BinaryTags.writeCompressed(Files.newOutputStream(file), tag);
    });
  }

  public static void writeTagFile(Path f, Consumer<CompoundTag> saveCallback) {
    writeFile(f, file -> {
      var tag = BinaryTags.compoundTag();
      saveCallback.accept(tag);
      BinaryTags.writeCompressed(Files.newOutputStream(file), tag);
    });
  }

  public static void writeJson(Path f, JsonElement element) {
    writeFile(f, file -> JsonUtils.writeFile(element, file));
  }

  public static void writeJsonFile(Path f, Consumer<JsonWrapper> saveCallback) {
    writeFile(f, file -> {
      var json = JsonWrapper.create();
      saveCallback.accept(json);
      JsonUtils.writeFile(json.getSource(), file);
    });
  }

  public interface IoReader<O> {

    O apply(Path file) throws IOException;
  }

  public interface IoWriter {

    void apply(Path file) throws IOException;
  }
}