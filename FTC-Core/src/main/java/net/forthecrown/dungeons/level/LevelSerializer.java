package net.forthecrown.dungeons.level;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.registry.Registries;
import net.kyori.adventure.key.Key;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public final class LevelSerializer {
    private LevelSerializer() {}

    private static final Logger LOGGER = Crown.logger();

    public static File getFile() {
        return new File(Crown.dataFolder(), "dungeon_levels.dat");
    }

    private static CompoundTag readFile() {
        File f = getFile();

        if(!f.exists()) {
            return new CompoundTag();
        }

        try {
            return NbtIo.readCompressed(f);
        } catch (IOException e) {
            LOGGER.error("Could not read dungeon level file", e);
            return null;
        }
    }

    public static void save() {
        CompoundTag tag = new CompoundTag();

        for (DungeonLevel l: Registries.DUNGEON_LEVELS) {
            CompoundTag lTag = new CompoundTag();
            l.save(lTag);

            tag.put(l.key().asString(), lTag);
        }

        try {
            File f = getFile();
            if(!f.exists()) {
                f.createNewFile();
            }

            NbtIo.writeCompressed(tag, f);
            LOGGER.info("Saved dungeon levels");
        } catch (IOException e) {
            LOGGER.error("Could not save dungeon levels", e);
        }
    }

    public static void load() {
        CompoundTag tag = readFile();
        if(tag == null) return;

        Registries.DUNGEON_LEVELS.clear();

        for (Map.Entry<String, Tag> e: tag.tags.entrySet()) {
            Key key = Keys.parse(e.getKey());
            CompoundTag lTag = (CompoundTag) e.getValue();

            DungeonLevelImpl level = new DungeonLevelImpl(key);
            level.load(lTag);

            Registries.DUNGEON_LEVELS.register(key, level);
        }

        LOGGER.info("Successfully loaded dungeon levels");
    }
}
