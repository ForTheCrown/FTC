package net.forthecrown.utils.io;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.minecraft.nbt.CompoundTag;

import java.nio.file.Path;

/**
 * An abstract class to make file serialization and deserialization easier
 */
public interface SerializableObject {
    /**
     * Saves the file
     */
    @OnSave
    void save();

    /**
     * Reloads the file
     */
    @OnLoad
    void reload();

    @Getter
    @RequiredArgsConstructor
    abstract class AbstractSerializer<T> implements SerializableObject {
        protected final Path filePath;

        protected abstract void load(T t);
        protected abstract void save(T t);
    }

    abstract class NbtDat extends AbstractSerializer<CompoundTag> {
        public NbtDat(Path filePath) {
            super(filePath);
        }

        @Override
        @OnSave
        public void save() {
            SerializationHelper.writeTagFile(filePath, this::save);
        }

        @Override
        @OnLoad
        public void reload() {
            SerializationHelper.readTagFile(filePath, this::load);
        }
    }

    abstract class Json extends AbstractSerializer<JsonWrapper> {
        public Json(Path filePath) {
            super(filePath);
        }

        @Override
        @OnSave
        public void save() {
            SerializationHelper.writeJsonFile(filePath, this::save);
        }

        @Override
        @OnLoad
        public void reload() {
            SerializationHelper.readJsonFile(filePath, this::load);
        }
    }
}