package net.forthecrown.text.format;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;

class DateFormatType implements TextFormatType {

  @Override
  public Component resolveArgument(Object value, String style) {
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

    return Text.valueOf(value);
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