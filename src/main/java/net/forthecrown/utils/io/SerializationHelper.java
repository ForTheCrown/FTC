package net.forthecrown.utils.io;

import com.google.gson.JsonObject;
import net.forthecrown.core.Crown;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class SerializationHelper {
    private SerializationHelper() {}

    private static final Logger LOGGER = Crown.logger();

    private static final IoReader<CompoundTag> TAG_READER = file -> NbtIo.readCompressed(Files.newInputStream(file));
    private static final IoReader<JsonObject> JSON_READER = JsonUtils::readFileObject;

    public static <T> void readFile(Path file, IoReader<T> reader, Consumer<T> loadCallback) {
        if (!Files.exists(file)) {
            return;
        }

        try {
            var t = reader.apply(file);
            loadCallback.accept(t);
        } catch (IOException e) {
            LOGGER.error("Error writing file: '" + file + "'", e);
        }
    }

    public static void readTagFile(Path file, Consumer<CompoundTag> loadCallback) {
        readFile(file, TAG_READER, loadCallback);
    }

    public static void readJsonFile(Path file, Consumer<JsonWrapper> loadCallback) {
        readFile(file, JSON_READER, object -> loadCallback.accept(JsonWrapper.wrap(object)));
    }

    public static void writeFile(Path file, IoWriter writer) {
        try {
            if (!Files.exists(file)) {
                var parent = file.getParent();

                if (!Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
            }

            writer.apply(file);
        } catch (IOException e) {
            LOGGER.error("Error writing file: '" + file + "'", e);
        }
    }

    public static void writeTagFile(Path f, Consumer<CompoundTag> saveCallback) {
        writeFile(f, file -> {
            var tag = new CompoundTag();
            saveCallback.accept(tag);
            NbtIo.writeCompressed(tag, Files.newOutputStream(file));
        });
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