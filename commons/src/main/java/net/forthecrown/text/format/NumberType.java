package net.forthecrown.text.format;

import static net.forthecrown.text.format.TextFormatTypes.DEFAULT;
import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import java.text.DecimalFormat;
import net.forthecrown.text.RomanNumeral;
import net.forthecrown.text.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

class NumberType implements TextFormatType {

  @Override
  public @NotNull Component resolve(Object value, String style, Audience audience) {
    if (!(value instanceof Number number)) {
      return DEFAULT.resolve(value, style, audience);
    }

    if (style.contains("-roman")) {
      return Component.text(RomanNumeral.arabicToRoman(number.longValue()));
    }

    if (style.contains("-floor")) {
      // Remove style argument, style may contain
      // a number format, or not, it just as to be empty lol
      style = style.replaceAll("-floor", "").trim();

      number = number.longValue();
    }

    return text(
        Strings.isNullOrEmpty(style)
            ? Text.NUMBER_FORMAT.format(number)
            : createFormat(style).format(number)
    );
  }

  private DecimalFormat createFormat(String pattern) {
    var format = new DecimalFormat(pattern);
    format.setGroupingUsed(true);
    format.setGroupingSize(3);

    return format;
  }
}