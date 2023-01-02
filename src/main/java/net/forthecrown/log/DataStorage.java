package net.forthecrown.log;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
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
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.Logger;

@Getter
public class DataStorage {

  private static final Logger LOGGER = FTC.getLogger();

  /* ----------------------- FILE NAME FORMATTERS ------------------------- */

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

      for (var p : stream) {
        try {
          YearMonth month = YearMonth.parse(
              p.getFileName().toString(),
              YEAR_MONTH_FORMATTER
          );

          if (minYear.isAfter(month)) {
            minYear = month;
          }
        } catch (DateTimeParseException ignored) {
        }
      }

      stream.close();
      LOGGER.debug("Min log date={}", minYear);
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

    return directory.resolve(
        FILENAME_FORMATTER.format(date) + ".ftc_log"
    );
  }

  public void loadLogs(ChronoLocalDate date, LogContainer container) {
    Path path = getLogFile(date);

    if (!Files.exists(path)) {
      return;
    }

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

  public void saveLogs(LocalDate date, LogContainer container) {
    Optional<JsonElement> result = container.serialize(JsonOps.INSTANCE)
        .resultOrPartial(LOGGER::error);

    if (result.isEmpty()) {
      return;
    }

    var path = getLogFile(date);

    try {
      SerializationHelper.ensureParentExists(path);
      saveBinaryLogs(path, container);
    } catch (IOException exc) {
      LOGGER.error("Couldn't write binary log file {}",
          path, exc
      );
    }
  }

  private void saveBinaryLogs(Path path, LogContainer container)
      throws IOException {
    DataLog[] logs = container.getLogs();
    LogFile file = new LogFile(logs);

    file.fillArrays();
    file.write(path);
  }

  public void loadForQuery(ChronoLocalDate date, QueryResultBuilder builder) {
    var path = getLogFile(date);

    var holder = builder.getQuery().getSchema();

    try {
      InputStream stream = Files.newInputStream(path);
      DataInputStream input = new DataInputStream(stream);

      var header = LogFile.readHeader(input);
      for (var p : header) {
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