package net.forthecrown.log;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import lombok.AccessLevel;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.registry.Keys;
import net.forthecrown.utils.ArrayIterator;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class LogContainer {
    private static final Logger LOGGER = FTC.getLogger();

    @Getter(AccessLevel.PACKAGE)
    private DataLog[] logs = DataLogs.EMPTY_LOG_ARR;

    public void log(LogSchema schema, LogEntry entry) {
        DataLogs.SCHEMAS
                .getHolderByValue(schema)
                .ifPresentOrElse(holder -> {
                    log(holder, entry);
                }, () -> {
                    LOGGER.warn("Unregistered schema found: {}", schema);
                });
    }

    public void log(@NotNull Holder<LogSchema> holder, @NotNull LogEntry entry) {
        Objects.requireNonNull(holder, "Holder");
        Objects.requireNonNull(entry, "Entry");

        logs = ObjectArrays.ensureCapacity(logs, holder.getId() + 1);
        DataLog log = logs[holder.getId()];

        if (log == null) {
            log = new DataLog(holder.getValue());
            logs[holder.getId()] = log;
        }

        log.add(entry);
    }

    public @Nullable DataLog getLog(@NotNull Holder<LogSchema> holder) {
        Objects.requireNonNull(holder, "Holder");

        if (holder.getId() >= logs.length) {
            return null;
        }

        return logs[holder.getId()];
    }

    public void setLog(@NotNull Holder<LogSchema> holder, @Nullable DataLog log) {
        Objects.requireNonNull(holder, "Holder");

        if (log == null) {
            if (holder.getId() < logs.length
                    && logs[holder.getId()] != null
            ) {
                logs[holder.getId()] = null;
            }

            return;
        }

        logs = ObjectArrays.ensureCapacity(logs, holder.getId() + 1);
        logs[holder.getId()] = log;
    }

    /* --------------------------- SERIALIZATION ---------------------------- */

    public <S> DataResult<S> serialize(DynamicOps<S> ops) {
        var logIt = ArrayIterator.unmodifiable(logs);

        if (!logIt.hasNext()) {
            return DataResult.success(ops.emptyMap());
        }

        RecordBuilder<S> builder = ops.mapBuilder();

        while (logIt.hasNext()) {
            int index = logIt.nextIndex();
            var next = logIt.next();

            DataLogs.SCHEMAS.getHolder(index)
                    .ifPresentOrElse(holder -> {
                        builder.add(holder.getKey(), next.serialize(ops));
                    }, () -> {
                        LOGGER.error("Unknown data log found at index {}",
                                index
                        );
                    });
        }

        return builder.build(ops.empty());
    }

    public <S> void deserialize(Dynamic<S> dynamic) {
        Map<String, Dynamic<S>> map = DataLogs.asMap(dynamic);

        for (var e: map.entrySet()) {
            if (!Keys.isValidKey(e.getKey())) {
                LOGGER.warn("Invalid key found in log container: '{}'",
                        e.getKey()
                );

                continue;
            }

            DataLogs.SCHEMAS
                    .getHolder(e.getKey())
                    .ifPresentOrElse(holder -> {
                        DataLog log = new DataLog(holder.getValue());
                        log.deserialize(e.getValue());

                        setLog(holder, log);
                    }, () -> {
                        LOGGER.warn("Unknown data log: '{}'", e.getKey());
                    });

        }
    }
}