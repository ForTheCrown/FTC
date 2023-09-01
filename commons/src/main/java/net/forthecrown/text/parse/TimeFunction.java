package net.forthecrown.text.parse;

import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.Getter;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.PeriodFormat;
import net.forthecrown.utils.Time;
import net.kyori.adventure.text.Component;

public class TimeFunction extends TextFunction {

  public static final String PRESENT = "present";

  public TimeFunction() {
    super(
        Pattern.compile("<t[:=](-?[0-9]+|(?:present(?:[-+][0-9]+[a-zA-Z]?)?))(?::([tTdDfFR]))?>")
    );
  }

  @Override
  public boolean test(TextContext context) {
    return context.has(ChatParseFlag.TIMESTAMPS);
  }

  @Override
  public Component format(MatchResult result, TextContext context) {
    String time = result.group(1);
    String unit = result.group(2);

    FormatterType type;

    if (Strings.isNullOrEmpty(unit)) {
      type = FormatterType.DEFAULT;
    } else {
      type = FormatterType.byChar(unit.charAt(0));

      if (type == null) {
        return null;
      }
    }

    long timestamp;

    if (time.startsWith(PRESENT)) {
      timestamp = System.currentTimeMillis();

      StringReader reader = new StringReader(time);
      reader.setCursor(PRESENT.length());
      reader.skipWhitespace();

      if (reader.canRead() && (reader.peek() == '-' || reader.peek() == '+')) {
        int multiplier = reader.peek() == '-' ? -1 : 1;
        reader.skip();

        try {
          Duration duration = ArgumentTypes.time().parse(reader);
          long offset = duration.toMillis() * multiplier;
          timestamp = timestamp + offset;
        } catch (CommandSyntaxException exc) {
          // Ignore
        }
      }
    } else {
      timestamp = Long.parseLong(time);
    }

    return type.format(timestamp);
  }

  @Getter
  enum FormatterType {
    TIME_SHORT ('t', "HH:mm"),
    TIME_LONG ('T', "HH:mm:ss"),

    DATE_SHORT ('d', "dd/MM/uuuu"),
    DATE_LONG ('D', "dd LLLL uuuu"),

    DATETIME_SHORT ('f', "dd LLLL uuuu HH:mm z"),
    DATETIME_LONG ('F', "EEEE, dd LLLL uuuu HH:mm z"),

    RELATIVE ('R', null);

    private static final FormatterType DEFAULT = DATETIME_LONG;

    private final char character;
    private final DateTimeFormatter formatter;

    FormatterType(char character, String pattern) {
      this.character = character;

      this.formatter = pattern == null
          ? null
          : DateTimeFormatter.ofPattern(pattern);
    }

    Component format(long time) {
      // No formatter = RELATIVE
      if (formatter == null) {
        if (Time.isPast(time)) {
          return PeriodFormat.timeStamp(time)
              .retainBiggest()
              .asComponent()
              .append(Component.text(" ago"));
        }

        return Component.text()
            .append(Component.text("in "))
            .append(
                PeriodFormat.timeStamp(time)
                    .retainBiggest()
                    .asComponent()
            )
            .build();
      }

      ZonedDateTime dateTime = Time.dateTime(time);
      return Component.text(
          formatter.format(dateTime)
      );
    }

    static FormatterType byChar(char c) {
      for (var t: values()) {
        if (t.character == c) {
          return t;
        }
      }

      return null;
    }
  }
}