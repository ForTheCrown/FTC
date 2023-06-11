package net.forthecrown.text.format;

import net.forthecrown.text.Text;
import net.forthecrown.text.UnitFormat;
import net.kyori.adventure.text.Component;

record UnitType(String unit) implements TextFormatType {

  @Override
  public Component resolveArgument(Object value, String style) {
    if (!(value instanceof Number number)) {
      return Text.valueOf(value);
    }

    return UnitFormat.unit(number, unit);
  }
}