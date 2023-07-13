package net.forthecrown.utils.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import net.forthecrown.Loggers;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import org.slf4j.Logger;
import org.tomlj.Toml;
import org.tomlj.TomlTable;

public final class SerializationHelper {
  private SerializationHelper() {}

  private static final Logger LOGGER = Loggers.getLogger();

  public static final IoReader<CompoundTag>
      TAG_READER = file -> BinaryTags.readCompressed(Files.newInputStream(file));

  public static final IoReader<JsonObject>
      JSON_READER = JsonUtils::readFileObject;

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
        t.errors().forEach(err -> LOGGER.error(err.toString()));
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
        result.errors().forEach(err -> LOGGER.error(err.toString()));
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

  public static boolean readAsJson(Path file, Consumer<JsonWrapper> callback) {
    String fName = file.getFileName().toString();

    if (fName.endsWith(".toml")) {
      return readTomlAsJson(file, callback);
    } else {
      return readJsonFile(file, callback);
    }
  }

  public static boolean readJsonFile(Path file, Consumer<JsonWrapper> loadCallback) {
    return readFile(file, JSON_READER, object -> loadCallback.accept(JsonWrapper.wrap(object)));
  }

  public static boolean writeFile(Path file, IoWriter writer) {
    try {
      PathUtil.ensureParentExists(file);
      writer.apply(file);
      return true;
    } catch (IOException e) {
      LOGGER.error("Error writing file: '" + file + "'", e);
      return false;
    }
  }

  public static boolean writeTag(Path f, CompoundTag tag) {
    return writeFile(f, file -> {
      BinaryTags.writeCompressed(Files.newOutputStream(file), tag);
    });
  }

  public static boolean writeTagFile(Path f, Consumer<CompoundTag> saveCallback) {
    return writeFile(f, file -> {
      var tag = BinaryTags.compoundTag();
      saveCallback.accept(tag);
      BinaryTags.writeCompressed(Files.newOutputStream(file), tag);
    });
  }

  public static boolean writeJson(Path f, JsonElement element) {
    return writeFile(f, file -> JsonUtils.writeFile(element, file));
  }

  public static boolean writeJsonFile(Path f, Consumer<JsonWrapper> saveCallback) {
    return writeFile(f, file -> {
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