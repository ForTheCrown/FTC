package net.forthecrown.log;

import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.module.OnDayChange;
import net.forthecrown.core.module.OnLoad;
import net.forthecrown.core.module.OnSave;
import net.forthecrown.utils.io.PathUtil;
import org.apache.commons.lang3.Range;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
public class LogManager {
    @Getter
    private static final LogManager instance = new LogManager();

    private static final Logger LOGGER = FTC.getLogger();

    /* -------------------------- INSTANCE FIELDS --------------------------- */

    private LocalDate date = LocalDate.now();

    private LogContainer logs = new LogContainer();

    private final DataStorage storage;

    private Range<ChronoLocalDate> logRange;

    /* ---------------------------- CONSTRUCTOR ----------------------------- */

    private LogManager() {
        storage = new DataStorage(PathUtil.getPluginDirectory("data"));
        logRange = Range.is(date);
    }

    /* ------------------------------ METHODS ------------------------------- */

    @OnDayChange
    void onDayChange(ZonedDateTime time) {
        save();

        date = time.toLocalDate();
        logs = new LogContainer();

        logRange = Range.between(logRange.getMinimum(), date);
    }

    public List<LogEntry> queryLogs(LogQuery query) {
        var searchRange = query.getSearchRange();

        if (!logRange.isOverlappedBy(searchRange)) {
            return ObjectLists.emptyList();
        }

        ChronoLocalDate d = searchRange.getMaximum();
        short safeGuard = 512;

        QueryResultBuilder builder = new QueryResultBuilder(query);

        // While within search range, query logs of specific day
        // and then move the date backwards by one
        while (searchRange.contains(d)) {
            --safeGuard;

            if (safeGuard < 0) {
                LOGGER.error(
                        "Query operation passed safeGuard loop limit! " +
                                "date={}, queryRange={}, searchDate={}",
                        date, searchRange, d,
                        new RuntimeException()
                );

                break;
            }

            // If current date, then don't load file,
            // File will most likely have invalid data,
            // use the loaded container
            if (d.compareTo(date) == 0) {
                var log = this.logs.getLog(query.getSchema());

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

            d = d.minus(1, ChronoUnit.DAYS);
        }

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
        logRange = Range.between(minDate, date);
    }
}