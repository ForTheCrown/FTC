package net.forthecrown.text.format;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Strings;
import java.text.DecimalFormat;
import net.forthecrown.text.RomanNumeral;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;

class NumberType implements TextFormatType {

  @Override
  public Component resolveArgument(Object value, String style) {
    if (!(value instanceof Number number)) {
      return Text.valueOf(value);
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

    if (style.contains("-floor")) {
      number = number.longValue();
      style = style.replaceAll("-floor", "")
          .trim();
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