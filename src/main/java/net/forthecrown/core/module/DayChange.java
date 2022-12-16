package net.forthecrown.core.module;

import net.forthecrown.core.FTC;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.text.format.PeriodFormat;
import org.apache.logging.log4j.Logger;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjuster;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * DayUpdate listens to a change in the day.
 */
public class DayChange extends ModuleService {
    /* ----------------------------- CONSTANTS ------------------------------ */

    private static final Logger LOGGER = FTC.getLogger();

    public static final TemporalAdjuster NEXT_DAY = temporal -> {
        return temporal.plus(1, DAYS)
                .with(HOUR_OF_DAY, 0)
                .with(MINUTE_OF_HOUR, 0)
                .with(SECOND_OF_MINUTE, 0)
                .with(MILLI_OF_SECOND, 1);
    };

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    private BukkitTask updateTask;

    DayChange() {
        super(OnDayChange.class);
    }

    @Override
    public void invoke(@Nullable Object instance, @NotNull Method m)
            throws Throwable
    {
        if (m.getParameterCount() == 0) {
            m.invoke(instance);
            return;
        }

        m.invoke(instance, ZonedDateTime.now());
    }

    @Override
    public Optional<String> testParams(Method m) {
        if (m.getParameterCount() == 0) {
            return Optional.empty();
        }

        if (m.getParameterCount() > 1) {
            return Optional.of("Expected 1 or 0 parameters");
        }

        Class param = m.getParameterTypes()[0];

        if (param != ZonedDateTime.class) {
            return Optional.of(
                    "Expected the 1 parameter to be a ZonedDateTime"
            );
        }

        return Optional.empty();
    }

    public void schedule() {
        // Cancel if previous task exists
        updateTask = Tasks.cancel(updateTask);
        // Find difference between now and tomorrow
        long difference = Time.timeUntil(getNextDayChange());

        LOGGER.info("DayUpdate scheduled, executing in: {}", PeriodFormat.of(difference));

        // Convert to ticks for bukkit scheduler
        difference = Time.millisToTicks(difference);

        // Run update on next day change and then run it every
        // 24 hours, aka once a day. It probably won't get ran
        // a second time cuz of daily restart, but whatever lol
        // future-proof :D
        updateTask = Tasks.runTimer(
                this,
                difference,
                Time.millisToTicks(TimeUnit.DAYS.toMillis(1))
        );
    }

    public static long getNextDayChange() {
        // Configure calendar to be at the start of the next day
        // So we can run day update exactly on time. As always,
        // there's probably a better way of doing this, but IDK lol
        ZonedDateTime time = ZonedDateTime.now();
        time = time.with(NEXT_DAY);

        return Time.toTimestamp(time);
    }
}