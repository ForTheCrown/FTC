package net.forthecrown.log;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
class QueryResultBuilder
        implements Consumer<LogEntry>, Predicate<LogEntry>
{
    private final LinkedList<LogEntry> result = new LinkedList<>();

    @Getter
    private final LogQuery query;

    @Override
    public void accept(LogEntry entry) {
        result.addFirst(entry);
    }

    @Override
    public boolean test(LogEntry entry) {
        return query.test(entry);
    }

    public boolean hasFoundEnough() {
        if (query.getMaxResults() == -1) {
            return false;
        }

        return result.size() >= query.getMaxResults();
    }

    public List<LogEntry> build() {
        return new ObjectArrayList<>(result);
    }
}