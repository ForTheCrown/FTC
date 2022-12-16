package net.forthecrown.utils.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.forthecrown.core.FTC;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

public final class SerializationHelper {
    private SerializationHelper() {}

    private static final Logger LOGGER = FTC.getLogger();

    public static final IoReader<CompoundTag>
            TAG_READER = file -> NbtIo.readCompressed(Files.newInputStream(file));

    public static final IoReader<JsonObject>
            JSON_READER = JsonUtils::readFileObject;

    public static void ensureParentExists(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        var parent = file.getParent();
        PathUtil.ensureDirectoryExists(parent).orThrow();
    }

    public static <T> DataResult<T> readFileObject(Path file, IoReader<T> reader) {
        if (!Files.exists(file)) {
            return Results.errorResult("File '%s' doesn't exist", file);
        }

        try {
            return DataResult.success(reader.apply(file));
        } catch (IOException e) {
            LOGGER.error("Error writing file: '" + file + "'", e);
            return DataResult.error(e.getMessage());
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

    public static boolean readTagFile(Path file, Consumer<CompoundTag> loadCallback) {
        return readFile(file, TAG_READER, loadCallback);
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
            NbtIo.writeCompressed(tag, Files.newOutputStream(file));
        });
    }

    public static void writeTagFile(Path f, Consumer<CompoundTag> saveCallback) {
        writeFile(f, file -> {
            var tag = new CompoundTag();
            saveCallback.accept(tag);
            NbtIo.writeCompressed(tag, Files.newOutputStream(file));
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