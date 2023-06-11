package net.forthecrown.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

public interface TextWriter {

  void write(ComponentLike text);

  void clear();

  default void field(ComponentLike field, ComponentLike value) {
    line(field.asComponent().applyFallbackStyle(getFieldStyle()));
    write(getFieldSeparator());

    if (!Text.isEmpty(value)) {
      write(value.asComponent().applyFallbackStyle(getFieldValueStyle()));
    }
  }

  default void field(String field, ComponentLike value) {
    field(Text.renderString(field), value);
  }

  default void field(String field, Object value) {
    field(Text.renderString(field), Text.valueOf(value));
  }

  default void line(String s) {
    line(Text.renderString(s));
  }

  default void line(String s, TextColor color) {
    line(Text.renderString(s).applyFallbackStyle(color));
  }

  default void line(String s, Style style) {
    line(Text.renderString(s).applyFallbackStyle(style));
  }

  default void line(ComponentLike text) {
    if (!isLineEmpty()) {
      newLine();
    }

    write(text);
  }

  void newLine();

  default void space() {
    write(Component.space());
  }

  default void write(String s, Style style) {
    write(Text.renderString(s).applyFallbackStyle(style));
  }

  default void write(String s, TextColor color) {
    write(Text.renderString(s).applyFallbackStyle(color));
  }

  default void write(String s) {
    write(Text.renderString(s));
  }

  default void formatted(ComponentLike format, Object... args) {
    write(Text.format(format.asComponent(), args));
  }

  default void formatted(String format, Object... args) {
    write(Text.format(format, args));
  }

  default void formatted(String format, TextColor color, Object... args) {
    write(Text.format(format, color, args));
  }

  default void formatted(String format, Style style, Object... args) {
    write(Text.format(format, style, args));
  }

  default void formattedLine(String format, Object... args) {
    line(Text.format(format, args));
  }

  default void formattedLine(String format, TextColor color, Object... args) {
    line(Text.format(format, color, args));
  }

  default void formattedLine(String format, Style style, Object... args) {
    line(Text.format(format, style, args));
  }

  default void formattedLine(ComponentLike like, Object... args) {
    line(Text.format(like.asComponent(), args));
  }

  String getString();

  String getPlain();

  default PrefixedWriter withPrefix(ComponentLike prefix) {
    return new PrefixedWriter(this, prefix.asComponent());
  }

  default PrefixedWriter withIndent(int indent) {
    return withPrefix(Component.text(" ".repeat(indent)));
  }

  default PrefixedWriter withIndent() {
    return withIndent(2);
  }

  boolean isLineEmpty();

  Style getFieldStyle();

  Style getFieldValueStyle();

  Component getFieldSeparator();

  void setFieldStyle(Style fieldStyle);

  void setFieldValueStyle(Style fieldValueStyle);

  void setFieldSeparator(Component fieldSeparator);
}