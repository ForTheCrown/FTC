package net.forthecrown.dungeons.level;

import lombok.Getter;
import net.forthecrown.dungeons.DungeonWorld;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.StructureFillConfig;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.SerializationHelper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;

@Getter
public class LevelDataStorage {
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral('_')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('_')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('_')
            .appendValue(ChronoField.HOUR_OF_DAY)
            .appendLiteral('_')
            .appendValue(ChronoField.MINUTE_OF_HOUR)
            .toFormatter();

    private final Path directory;
    private final Path archiveDirectory;

    private final Path activeLevel;

    LevelDataStorage(Path directory) {
        this.directory = directory;
        this.archiveDirectory = directory.resolve("archives");
        this.activeLevel = directory.resolve("level.dat");
    }

    public Path getPath(long creationTime, int i) {
        LocalDateTime localTime = Time.localTime(creationTime);
        String strPath = FORMATTER.format(localTime);

        if (i > 0) {
            strPath += " (" + i + ")";
        }

        strPath += ".dat";
        return archiveDirectory.resolve(strPath);
    }

    public void archiveLevel(DungeonLevel level, long creationTime) {
        Path path = null;
        int i = 0;

        while (path == null || Files.exists(path)) {
            path = getPath(creationTime, i);
        }

        BlockStructure structure = new BlockStructure();
        StructureFillConfig config = StructureFillConfig.builder()
                .blockPredicate(block -> !block.getType().isAir())
                .area(level.getChunkMap().getTotalArea().toWorldBounds(DungeonWorld.get()))
                .build();

        structure.fill(config);

        var header = structure.getHeader();
        header.putString("createdDate", JsonUtils.DATE_FORMAT.format(new Date(creationTime)));

        SerializationHelper.writeTagFile(path, structure::save);
    }
}