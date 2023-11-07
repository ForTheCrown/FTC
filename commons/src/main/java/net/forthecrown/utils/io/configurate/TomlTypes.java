package net.forthecrown.utils.io.configurate;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.function.Predicate;
import net.forthecrown.utils.Time;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

class TomlTypes {

  static final TypeSerializerCollection SERIALIZERS = TypeSerializerCollection.defaults()
      .childBuilder()
      .register(new LocalDateTimeSerializer())
      .register(new LocalTimeSerializer())
      .register(new LocalDateSerializer())
      .register(new OffsetDateTimeSerializer())
      .build();

  static abstract class TemporalSerializer<T extends TemporalAccessor> extends ScalarSerializer<T> {

    private final Class<T> type;

    protected TemporalSerializer(Class<T> type) {
      super(type);
      this.type = type;
    }

    protected abstract T from(TemporalAccessor accessor);
    protected abstract T parse(String str);
    protected abstract T fromEpoch(long value);

    @Override
    public T deserialize(Type type, Object obj) throws SerializationException {
      if (this.type.isInstance(obj)) {
        return (T) obj;
      }

      if (obj instanceof TemporalAccessor accessor) {
        return from(accessor);
      }

      if (obj instanceof String str) {
        return parse(str);
      }

      if (obj instanceof Number number) {
        return fromEpoch(number.longValue());
      }

      throw new SerializationException(
          "Don't know how to deserialize " + obj + " into a " + this.type.getSimpleName()
      );
    }

    @Override
    protected Object serialize(T item, Predicate<Class<?>> typeSupported) {
      if (typeSupported.test(type)) {
        return item;
      } else {
        return item.toString();
      }
    }
  }

  static class OffsetDateTimeSerializer extends TemporalSerializer<OffsetDateTime> {

    public OffsetDateTimeSerializer() {
      super(OffsetDateTime.class);
    }

    @Override
    protected OffsetDateTime from(TemporalAccessor accessor) {
      return OffsetDateTime.from(accessor);
    }

    @Override
    protected OffsetDateTime parse(String str) {
      return OffsetDateTime.parse(str);
    }

    @Override
    protected OffsetDateTime fromEpoch(long value) {
      ZonedDateTime time = Time.dateTime(value);
      return OffsetDateTime.from(time);
    }
  }

  static class LocalDateSerializer extends TemporalSerializer<LocalDate> {

    public LocalDateSerializer() {
      super(LocalDate.class);
    }

    @Override
    protected LocalDate from(TemporalAccessor accessor) {
      return LocalDate.from(accessor);
    }

    @Override
    protected LocalDate parse(String str) {
      return LocalDate.parse(str);
    }

    @Override
    protected LocalDate fromEpoch(long value) {
      return LocalDate.ofEpochDay(value);
    }
  }

  static class LocalTimeSerializer extends TemporalSerializer<LocalTime> {

    public LocalTimeSerializer() {
      super(LocalTime.class);
    }

    @Override
    protected LocalTime from(TemporalAccessor accessor) {
      return LocalTime.from(accessor);
    }

    @Override
    protected LocalTime parse(String str) {
      return LocalTime.parse(str);
    }

    @Override
    protected LocalTime fromEpoch(long value) {
      return LocalTime.ofSecondOfDay(value);
    }
  }

  static class LocalDateTimeSerializer extends TemporalSerializer<LocalDateTime> {

    protected LocalDateTimeSerializer() {
      super(LocalDateTime.class);
    }

    @Override
    protected LocalDateTime from(TemporalAccessor accessor) {
      return LocalDateTime.from(accessor);
    }

    @Override
    protected LocalDateTime parse(String str) {
      return LocalDateTime.parse(str);
    }

    @Override
    protected LocalDateTime fromEpoch(long value) {
      return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }
  }
}
