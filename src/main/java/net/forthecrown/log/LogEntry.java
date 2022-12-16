package net.forthecrown.log;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.registry.Holder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@EqualsAndHashCode
@RequiredArgsConstructor
public class LogEntry {
    final Object[] values;

    @Setter @Getter
    @Accessors(chain = true)
    long date;

    public static LogEntry of(Holder<LogSchema> holder) {
        return of(holder.getValue());
    }

    public static LogEntry of(LogSchema schema) {
        return new LogEntry(new Object[schema.getFields().length])
                .setDate(System.currentTimeMillis());
    }

    public <T> LogEntry set(@NotNull SchemaField<T> field, @Nullable T value) {
        values[field.id()] = value;
        return this;
    }

    public <T> @Nullable T get(@NotNull SchemaField<T> field) {
        return (T) values[field.id()];
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + Arrays.toString(values);
    }
}