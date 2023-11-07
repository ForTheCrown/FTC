package net.forthecrown.text.format;

import static net.forthecrown.text.format.TextFormatTypes.DEFAULT;

import net.forthecrown.text.Text;
import net.forthecrown.text.UnitFormat;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

record UnitType(String unit) implements TextFormatType {

  @Override
  public @NotNull Component resolve(@NotNull Object value, @NotNull String style, Audience viewer) {
    if (!(value instanceof Number number)) {
      return Text.valueOf(value, viewer);
    }

    return UnitFormat.unit(number, unit);
  }
}