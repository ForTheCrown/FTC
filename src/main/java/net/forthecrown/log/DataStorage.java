package net.forthecrown.log;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Optional;

@Getter
public class DataStorage {
    private static final Logger LOGGER = FTC.getLogger();

    /* ----------------------- FILE NAME FORMATTERS ------------------------- */

    public static final boolean USE_BINARY_FORMAT = true;

    public static final DateTimeFormatter FILENAME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral("_")
            .appendText(ChronoField.DAY_OF_WEEK, TextStyle.FULL_STANDALONE)
            .toFormatter();

    public final DateTimeFormatter YEAR_MONTH_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR)
            .appendLiteral("_")
            .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL_STANDALONE)
            .toFormatter();

    private final Path directory;

    DataStorage(Path directory) {
        this.directory = directory;
    }

    /* ------------------------------ METHODS ------------------------------- */

    public LocalDate findMinLog() {
        try {
            if (PathUtils.isEmptyDirectory(directory)) {
                return LocalDate.now();
            }

            YearMonth minYear = YearMonth.now();

            var stream = Files.newDirectoryStream(directory);

            for (var p: stream) {
                try {
                    YearMonth month = YearMonth.parse(
                            p.getFileName().toString(),
                            YEAR_MONTH_FORMATTER
                    );

                    if (month.getYear() < minYear.getYear()
                            && month.getMonthValue() < minYear.getMonthValue()
                    ) {
                        minYear = month;
                    }
                } catch (DateTimeParseException ignored) {}
            }

            stream.close();
            return LocalDate.of(minYear.getYear(), minYear.getMonth(), 1);
        } catch (IOException exc) {
            LOGGER.error(exc);
        }

        return LocalDate.now();
    }

    public Path getDirectory(ChronoLocalDate date) {
        return directory.resolve(YEAR_MONTH_FORMATTER.format(date));
    }

    public Path getLogFile(ChronoLocalDate date) {
        var directory = getDirectory(date);

        Path textPath = directory.resolve(
                FILENAME_FORMATTER.format(date) + ".json"
        );

        if (Files.exists(textPath) || !USE_BINARY_FORMAT) {
            return textPath;
        }

        return directory.resolve(
                FILENAME_FORMATTER.format(date) + ".ftc_log"
        );
    }

    public void loadLogs(ChronoLocalDate date, LogContainer container) {
        Path path = getLogFile(date);

        if (!Files.exists(path)) {
            return;
        }

        if (path.toString().endsWith(".json")) {
            SerializationHelper.readJsonFile(path, wrapper -> {
                container.deserialize(
                        new Dynamic<>(JsonOps.INSTANCE, wrapper.getSource())
                );
            });
        } else {
            try {
                InputStream stream = Files.newInputStream(path);
                DataInputStream input = new DataInputStream(stream);

                LogFile.readLog(input, container);

                input.close();
                stream.close();
            } catch (IOException exc) {
                LOGGER.error("Couldn't read binary JSON file {}",
                        path, exc
                );
            }
        }
    }

    public void saveLogs(LocalDate date, LogContainer container) {
        Optional<JsonElement> result = container.serialize(JsonOps.INSTANCE)
                .resultOrPartial(LOGGER::error);

        if (result.isEmpty()) {
            return;
        }

        var path = getLogFile(date);

        if (path.toString().endsWith(".json")) {
            SerializationHelper.writeJson(getLogFile(date), result.get());
        } else {
            try {
                SerializationHelper.ensureParentExists(path);
                saveBinaryLogs(path, container);
            } catch (IOException exc) {
                LOGGER.error("Couldn't write binary log file {}",
                        path, exc
                );
            }
        }
    }

    private void saveBinaryLogs(Path path, LogContainer container)
            throws IOException
    {
        DataLog[] logs = container.getLogs();
        LogFile file = new LogFile(logs);

        file.fillArrays();
        file.write(path);
    }

    public void loadForQuery(ChronoLocalDate date, QueryResultBuilder builder) {
        var path = getLogFile(date);

        var holder = builder.getQuery().getSchema();

        if (path.toString().endsWith(".json")) {
            var container = new LogContainer();
            loadLogs(date, container);

            var log = container.getLog(holder);

            if (log != null) {
                log.performQuery(builder);
            }
            return;
        }

        try {
            InputStream stream = Files.newInputStream(path);
            DataInputStream input = new DataInputStream(stream);

            var header = LogFile.readHeader(input);
            for (var p: header) {
                if (!p.section.equals(holder.getKey())) {
                    continue;
                }

                input.skipNBytes(p.offset);
                LogFile.readQuery(input, holder.getValue(), builder, p);

                break;
            }

            input.close();
            stream.close();
        } catch (IOException exc) {
            LOGGER.error("Error reading binary log file {}",
                    path, exc
            );
        }
    }
}