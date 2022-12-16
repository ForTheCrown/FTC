package net.forthecrown.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.utils.Util;
import org.apache.commons.lang3.Range;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class LogQuery implements Predicate<LogEntry> {
    private final Predicate[] predicates;
    private final Holder<LogSchema> schema;
    private final Range<ChronoLocalDate> searchRange;

    private final Predicate<LogEntry> entryPredicate;

    private final int maxResults;

    @Override
    public boolean test(LogEntry entry) {
        for (var f: schema.getValue().getFields()) {
            var predicates = this.predicates[f.id()];

            if (predicates == null) {
                continue;
            }

            try {
                if (!predicates.test(entry.get(f))) {
                    return false;
                }
            } catch (Throwable t) {
                FTC.getLogger().error(
                        "Error testing field '{}' on entry, value={}",
                        f.name(),
                        entry.get(f),
                        t
                );
                return false;
            }
        }

        return entryPredicate == null
                || entryPredicate.test(entry);
    }

    public static Builder<?> builder(Holder<LogSchema> schema) {
        return new Builder<>(schema);
    }

    @Setter
    @Accessors(fluent = true, chain = true)
    public static class Builder<F> {
        private final Predicate[] predicates;
        private final Holder<LogSchema> schema;

        private Predicate<LogEntry> entryPredicate;

        private int maxResults = Integer.MAX_VALUE;
        private Range<ChronoLocalDate> queryRange;

        public Builder(Holder<LogSchema> schema) {
            this.schema = schema;
            this.predicates = new Predicate[schema.getValue().getFields().length];
            this.queryRange = Range.is(LocalDate.now());
        }

        private SchemaField lastField;

        public <T> Builder<T> field(SchemaField<T> field) {
            if (!schema.getValue().contains(field)) {
                throw Util.newException(
                        "Field {}, id={} is not in the {} schema",
                        field.name(), field.id(),
                        schema.getKey()
                );
            }

            this.lastField = field;
            return (Builder<T>) this;
        }

        public Builder<F> set(Predicate<F> predicate) {
            Objects.requireNonNull(lastField, "Field not set");
            predicates[lastField.id()] = predicate;
            return this;
        }

        public Builder<F> addOr(Predicate<F> predicate) {
            return _add(predicate, Predicate::or);
        }

        public Builder<F> add(Predicate<F> predicate) {
            return  _add(predicate, Predicate::and);
        }

        private Builder<F> _add(Predicate<F> predicate,
                                BiFunction<Predicate<F>, Predicate<F>, Predicate<F>> combiner
        ) {
            Objects.requireNonNull(lastField, "Field not set");

            Predicate<F> existing = predicates[lastField.id()];

            if (existing != null) {
                predicates[lastField.id()] = combiner.apply(existing, predicate);
            } else {
                predicates[lastField.id()] = predicate;
            }

            return this;
        }

        public LogQuery build() {
            return new LogQuery(
                    predicates,
                    schema,
                    queryRange,
                    entryPredicate,
                    maxResults
            );
        }
    }
}