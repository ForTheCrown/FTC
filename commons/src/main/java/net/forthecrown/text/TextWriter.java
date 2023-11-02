package net.forthecrown.text;

import net.forthecrown.text.format.FormatBuilder;
import net.forthecrown.text.placeholder.PlaceholderRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Nullable;

public interface TextWriter extends ComponentLike {

  @Nullable
  Audience viewer();

  void viewer(@Nullable Audience audience);

  PlaceholderRenderer placeholders();

  void placeholders(PlaceholderRenderer renderer);

  void write(ComponentLike text);

  void clear();

  default void field(Object field) {
    line(Text.valueOf(field, viewer()).applyFallbackStyle(getFieldStyle()));
    write(getFieldSeparator().applyFallbackStyle(getFieldStyle()));
  }

  default void fieldSameLine(Object field, Object value) {
    write(Text.valueOf(field, viewer()).applyFallbackStyle(getFieldStyle()));
    write(getFieldSeparator().applyFallbackStyle(getFieldStyle()));
    fieldValue(value);
  }

  default void field(Object field, Object value) {
    field(field);
    fieldValue(value);
  }

  default void fieldValue(Object value) {
    Component valueText = value == null ? null : Text.valueOf(value, viewer());

    if (!Text.isEmpty(valueText)) {
      write(valueText.applyFallbackStyle(getFieldValueStyle()));
    }
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
    write(
        FormatBuilder.builder()
            .setViewer(viewer())
            .setFormat(format)
            .setArguments(args)
            .asComponent()
    );
  }

  default void formatted(String format, Object... args) {
    write(
        FormatBuilder.builder()
            .setViewer(viewer())
            .setFormat(format)
            .setArguments(args)
            .asComponent()
    );
  }

  default void formatted(String format, TextColor color, Object... args) {
    write(
        FormatBuilder.builder()
            .setViewer(viewer())
            .setFormat(format, color)
            .setArguments(args)
            .asComponent()
    );
  }

  default void formatted(String format, Style style, Object... args) {
    write(
        FormatBuilder.builder()
            .setViewer(viewer())
            .setFormat(format, style)
            .setArguments(args)
            .asComponent()
    );
  }

  default void formattedLine(String format, Object... args) {
    line(
        FormatBuilder.builder()
            .setViewer(viewer())
            .setFormat(format)
            .setArguments(args)
            .asComponent()
    );
  }

  default void formattedLine(String format, TextColor color, Object... args) {
    line(
        FormatBuilder.builder()
            .setViewer(viewer())
            .setFormat(format, color)
            .setArguments(args)
            .asComponent()
    );
  }

  default void formattedLine(String format, Style style, Object... args) {
    line(
        FormatBuilder.builder()
            .setViewer(viewer())
            .setFormat(format, style)
            .setArguments(args)
            .asComponent()
    );
  }

  default void formattedLine(ComponentLike like, Object... args) {
    line(FormatBuilder.builder().setViewer(viewer()).setFormat(like).setArguments(args).asComponent());
  }

  default void formattedField(Object field, String valueFormat, Style style, Object... args) {
    field(
        Text.valueOf(field),

        FormatBuilder.builder()
            .setArguments(args)
            .setFormat(valueFormat, style)
            .setViewer(viewer())
            .asComponent()
    );
  }

  default void formattedField(Object field, String valueFormat, TextColor color, Object... args) {
    field(
        Text.valueOf(field),

        FormatBuilder.builder()
            .setArguments(args)
            .setFormat(valueFormat, color)
            .setViewer(viewer())
            .asComponent()
    );
  }

  default void formattedField(Object field, String valueFormat, Object... args) {
    field(
        Text.valueOf(field, viewer()),

        FormatBuilder.builder()
            .setArguments(args)
            .setFormat(valueFormat)
            .setViewer(viewer())
            .asComponent()
    );
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