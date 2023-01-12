package net.forthecrown.log;

import co.aikar.timings.Timing;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.utils.io.PathUtil;
import org.apache.logging.log4j.Logger;

@Getter
public class LogManager {

  @Getter
  private static final LogManager instance = new LogManager();

  private static final Logger LOGGER = FTC.getLogger();

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  private LocalDate date = LocalDate.now();

  private LogContainer logs = new LogContainer();

  private final DataStorage storage;

  private DateRange logRange;

  private final Timing queryTiming;

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  private LogManager() {
    storage = new DataStorage(PathUtil.getPluginDirectory("data"));
    logRange = DateRange.exact(date);
    queryTiming = FTC.timing("Log Query");
  }

  /* ------------------------------ METHODS ------------------------------- */

  @OnDayChange
  void onDayChange(ZonedDateTime time) {
    save();

    date = time.toLocalDate();
    logs = new LogContainer();

    logRange = logRange.encompassing(date);
  }

  public List<LogEntry> queryLogs(LogQuery query) {
    DateRange searchRange = query.getSearchRange();

    if (!logRange.overlaps(searchRange)) {
      return ObjectLists.emptyList();
    }

    QueryResultBuilder builder = new QueryResultBuilder(query);
    queryTiming.startTiming();
    searchRange = logRange.overlap(searchRange);

    // While within search range, query logs of specific day
    // and then move the date backwards by one
    for (LocalDate d : searchRange) {
      // If current date, then don't load file,
      // File will most likely have invalid data,
      // use the loaded container
      if (d.compareTo(date) == 0) {
        DataLog log = this.logs.getLog(query.getSchema());

        if (log != null) {
          log.performQuery(builder);
        }
      }
      // If the file don't exist, nothing to look for
      else if (Files.exists(storage.getLogFile(d))) {
        // Query log file while loading it lol
        storage.loadForQuery(d, builder);
      }


      // If we've found more than the max requested results,
      // then stop looking for more
      if (builder.hasFoundEnough()) {
        break;
      }
    }

    queryTiming.stopTiming();
    return builder.build();
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  @OnSave
  public void save() {
    storage.saveLogs(date, logs);
  }

  @OnLoad
  public void load() {
    logs = new LogContainer();
    storage.loadLogs(date, logs);

    LocalDate minDate = storage.findMinLog();
    logRange = DateRange.between(minDate, date);
  }
}