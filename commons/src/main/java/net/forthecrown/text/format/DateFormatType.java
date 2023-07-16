package net.forthecrown.text.format;

import static net.forthecrown.text.format.TextFormatTypes.DEFAULT;
import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;
import net.forthecrown.text.Text;
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

    if (value instanceof ChronoZonedDateTime dateTime) {
      return format(dateTime.toInstant().toEpochMilli(), style);
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