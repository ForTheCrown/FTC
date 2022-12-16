package net.forthecrown.log;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class DataLog {
    private static final Comparator<LogEntry>
            TIME_COMPARATOR = Comparator.comparingLong(LogEntry::getDate);

    private static final Logger LOGGER = FTC.getLogger();

    private final LogSchema schema;
    private final List<LogEntry> entries = new ObjectArrayList<>();

    public void performQuery(QueryResultBuilder builder) {
        entries.stream().filter(builder).forEach(builder);
    }

    public void add(LogEntry entry) {
        if (entries.isEmpty()) {
            entries.add(entry);
            return;
        }

        int insertIndex = Collections.binarySearch(
                entries,
                entry,
                TIME_COMPARATOR
        );

        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }

        entries.add(insertIndex, entry);
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public <S> S serialize(DynamicOps<S> ops) {
        return ops.createList(
                entries.stream()
                        .map(entry -> {
                            return schema.serialize(ops, entry)
                                    .resultOrPartial(LOGGER::warn)
                                    .orElse(null);
                        })

                        .filter(Objects::nonNull)
        );
    }

    public <S> void deserialize(Dynamic<S> dynamic) {
        entries.clear();

        entries.addAll(
                dynamic.asList(dynamic1 -> {
                    return schema.deserialize(dynamic1)
                            .resultOrPartial(LOGGER::error)
                            .orElseThrow();
                })
        );
    }
}