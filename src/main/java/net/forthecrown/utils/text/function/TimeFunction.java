package net.forthecrown.utils.text.function;

import static net.forthecrown.utils.text.ChatParser.FLAG_LINKS;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.Getter;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.text.TextFunction;
import net.forthecrown.utils.text.format.PeriodFormat;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class TimeFunction extends TextFunction {

  public TimeFunction() {
    super(FLAG_LINKS, Pattern.compile("<t[:=][0-9]+(:[tTdDfFR])?>"));
  }

  @Override
  public @Nullable Component render(MatchResult group, int flags)
      throws CommandSyntaxException {
    String input = group.group();

    // Skip '<t:' and trailing '>'
    input = input.substring(3, input.length() - 1);
    FormatterType type = FormatterType.DEFAULT;
    String replaced = input.replaceAll("^[0-9]+", "");

    if (!replaced.isBlank()) {
      input = input.substring(0, input.length() - replaced.length());

      type = FormatterType.byChar(replaced.charAt(1));
      assert type != null;
    }

    long value = Long.parseLong(input);
    return type.format(value);
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