package net.forthecrown.dungeons;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.dungeons.level.DungeonLevel;
import net.forthecrown.utils.io.PathUtil;

import java.nio.file.Path;

@Getter
public class Dungeons {
    private static final Dungeons inst = new Dungeons();

    private final Path directory;

    @Getter @Setter
    private DungeonLevel currentLevel;

    public Dungeons() {
        this.directory = PathUtil.getPluginDirectory("dungeons");
    }

    public static Dungeons get() {
        return inst;
    }

    private static void init() {
        // TODO: load current level, register save callback, and potentially add to day change listener
    }
}