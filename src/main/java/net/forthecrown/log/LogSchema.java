package net.forthecrown.log;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMaps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.core.registry.Holder;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public class LogSchema {
    private static final Logger LOGGER = FTC.getLogger();

    private final SchemaField[] fields;

    private final Map<String, SchemaField>
            byName = new Object2ObjectOpenHashMap<>();

    private final short version;
    private final Short2ObjectMap<List<LogDataFixer>> updaters;

    private LogSchema(Builder builder) {
        this.fields = builder.fields.toArray(SchemaField[]::new);
        this.version = builder.version;
        this.updaters = Short2ObjectMaps.unmodifiable(builder.updaters);

        for (var f: Validate.noNullElements(fields)) {
            byName.put(f.name(), f);
        }
    }

    public static Builder builder(String name) {
        return builder(name, (short) 1);
    }

    public static Builder builder(String name, short version) {
        return new Builder(name, version);
    }

    public <S> Dynamic<S> update(Dynamic<S> dynamic, short oldVersion) {
        if (oldVersion == version) {
            return dynamic;
        }

        if (oldVersion > version) {
            LOGGER.warn(
                    "Cannot update entry with greater version than schema " +
                            "({} > {})",
                    oldVersion, version
            );

            return dynamic;
        }

        for (short i = (short) (oldVersion + 1); i <= version; i++) {
            var updaters = getUpdaters().get(i);

            if (updaters == null || updaters.isEmpty()) {
                continue;
            }

            for (var u: updaters) {
                dynamic = u.update(dynamic);
            }
        }

        return dynamic;
    }

    public <S> DataResult<S> serialize(DynamicOps<S> ops, LogEntry entry) {
        var builder = ops.mapBuilder();

        for (SchemaField<Object> field: getFields()) {
            Object value = entry.get(field);

            if (value == null) {
                continue;
            }

            builder.add(
                    field.name(),
                    field.type().encodeStart(ops, value)
            );
        }

        return builder.build((S) null);
    }

    public <S> DataResult<LogEntry> deserialize(Dynamic<S> dynamic) {
        var map = DataLogs.asMap(dynamic);
        LogEntry entry = LogEntry.of(this);

        for (var e: map.entrySet()) {
            String name = e.getKey();
            SchemaField<Object> field = byName.get(name);

            if (field == null) {
                LOGGER.warn("Unknown field found: {}", name);
                continue;
            }

            DataResult<Object> res = field.type()
                    .decode(e.getValue())
                    .map(Pair::getFirst);

            res.result().ifPresentOrElse(o -> {
                entry.set(field, o);
            }, () -> {
                LOGGER.warn("Couldn't read field '{}': '{}'",
                        name, res.error().get().message()
                );
            });
        }

        return DataResult.success(entry);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof LogSchema schema)) {
            return false;
        }

        return Arrays.equals(getFields(), schema.getFields());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getFields());
    }

    public <T> boolean contains(SchemaField<T> field) {
        if (field.id() >= fields.length) {
            return false;
        }

        return Objects.equals(field, fields[field.id()]);
    }

    @RequiredArgsConstructor
    public static class Builder {
        private final String name;
        private final List<SchemaField> fields = new ObjectArrayList<>();

        private final Short2ObjectMap<List<LogDataFixer>>
                updaters = new Short2ObjectOpenHashMap<>();

        private final short version;

        public <T> SchemaField<T> add(String name, Codec<T> codec) {
            SchemaField<T> field = new SchemaField<>(
                    name, fields.size(), codec
            );

            fields.add(field);
            return field;
        }

        public <T> Builder addField(String name, Codec<T> codec) {
            add(name, codec);
            return this;
        }

        public Builder addUpdate(int version, LogDataFixer fixer) {
            var list = updaters.computeIfAbsent(
                    (short) version,
                    s -> new ObjectArrayList<>()
            );

            list.add(fixer);
            return this;
        }

        public LogSchema build() {
            return new LogSchema(this);
        }

        public Holder<LogSchema> register() {
            return DataLogs.SCHEMAS.register(name, build());
        }
    }
}