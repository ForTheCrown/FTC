package net.forthecrown.text.format;

import static net.forthecrown.text.format.TextFormatTypes.DEFAULT;
import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import net.forthecrown.text.Text;
import net.forthecrown.utils.Time;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

class DateFormatType implements TextFormatType {

  @Override
  public @NotNull Component resolve(Object value, String style, Audience audience) {
    if (value instanceof Number number) {
      long timeStamp = number.longValue();
      return format(timeStamp, style);
    }

    if (value instanceof Date date) {
      return format(date, style);
    }

    if (value instanceof TemporalAccessor acc) {
      TemporalAccessor formatObject;

      if (acc instanceof Instant instant) {
        formatObject = Time.dateTime(instant.toEpochMilli());
      } else {
        formatObject = acc;
      }

      DateTimeFormatter formatter;

      if (Strings.isNullOrEmpty(style)) {
        formatter = Text.DATE_TIME_FORMATTER;
      } else {
        formatter = DateTimeFormatter.ofPattern(style);
      }

      String str = formatter.format(formatObject);
      return text(str);
    }

    return DEFAULT.resolve(value, style, audience);
  }

  private Component format(long l, String style) {
    return format(new Date(l), style);
  }

  private Component format(Date date, String style) {
    return text(
        Strings.isNullOrEmpty(style)
            ? Text.DATE_FORMAT.format(date)
            : new SimpleDateFormat(style).format(date)
    );
  }
}