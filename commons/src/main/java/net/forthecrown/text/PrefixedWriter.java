package net.forthecrown.text;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class PrefixedWriter implements TextWriter {

  private final TextWriter base;
  private final Component prefix;

  public PrefixedWriter(TextWriter other, Component prefix) {
    this.base = other;
    this.prefix = prefix;
  }

  @Override
  public void write(ComponentLike text) {
    if (isLineEmpty()) {
      base.write(prefix);
    }

    base.write(text);
  }

  @Override
  public void clear() {
    base.clear();
  }

  @Override
  public void newLine() {
    base.newLine();
  }

  @Override
  public String getString() {
    return base.getString();
  }

  @Override
  public String getPlain() {
    return base.getPlain();
  }

  @Override
  public boolean isLineEmpty() {
    return base.isLineEmpty();
  }

  @Override
  public Style getFieldStyle() {
    return base.getFieldStyle();
  }

  @Override
  public Style getFieldValueStyle() {
    return base.getFieldValueStyle();
  }

  @Override
  public Component getFieldSeparator() {
    return base.getFieldSeparator();
  }

  @Override
  public void setFieldStyle(Style fieldStyle) {
    base.setFieldStyle(fieldStyle);
  }

  @Override
  public void setFieldValueStyle(Style fieldValueStyle) {
    base.setFieldValueStyle(fieldValueStyle);
  }

  @Override
  public void setFieldSeparator(Component fieldSeparator) {
    base.setFieldSeparator(fieldSeparator);
  }

  @Override
  public PrefixedWriter withPrefix(ComponentLike prefix) {
    return new PrefixedWriter(base, Component.textOfChildren(this.prefix, prefix));
  }

  @Override
  public @Nullable Audience viewer() {
    return base.viewer();
  }

  @Override
  public void viewer(@Nullable Audience audience) {
    base.viewer(audience);
  }

  @Override
  public @NotNull Component asComponent() {
    return base.asComponent();
  }
}