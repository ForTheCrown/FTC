package net.forthecrown.text.format;

import java.util.Objects;
import net.forthecrown.text.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public class FormatBuilder implements ComponentLike {

  private Component format;
  private Object[] arguments;
  private Audience viewer;

  public static FormatBuilder builder() {
    return new FormatBuilder();
  }

  public FormatBuilder setFormat(String format) {
    return setFormat(Text.renderString(format));
  }

  public FormatBuilder setFormat(String base, TextColor color) {
    return setFormat(Text.renderString(base).color(color));
  }

  public FormatBuilder setFormat(String base, Style style) {
    return setFormat(Text.renderString(base).style(style));
  }

  public FormatBuilder setFormat(ComponentLike base) {
    this.format = base.asComponent();
    return this;
  }

  public FormatBuilder setArguments(Object... arguments) {
    this.arguments = arguments;
    return this;
  }

  public FormatBuilder setViewer(Audience viewer) {
    this.viewer = viewer;
    return this;
  }

  @Override
  @NotNull
  public Component asComponent() {
    Objects.requireNonNull(format, "Format base not set");

    if (arguments == null || arguments.length < 1) {
      return format;
    }

    return new ComponentFormat(format, arguments, viewer).asComponent();
  }
}
