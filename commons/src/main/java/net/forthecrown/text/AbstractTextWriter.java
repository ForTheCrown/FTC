package net.forthecrown.text;

import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Chat Writer is a common interface for writing multi-line components, blocks of texts, formatted
 * info lists or whatever else to a common interface which could then be displayed onto whatever the
 * writer implementation was written for.
 * <p>
 * Currently, there are text writers like {@link BufferedTextWriter} which depend on {@link #onNewLine()}
 * being called appropriately, the issue this faces however, is that currently, if
 * {@link #write(ComponentLike)} is called with a text that is exactly equal to
 * {@link Component#newline()} then it detects the new line, else it will fail to detect any
 * newlines being written
 * <p>
 * {@link TextWriters} provides static methods for creating these writers.
 *
 * @see TextWriters
 */
public abstract class AbstractTextWriter implements ComponentLike, TextWriter {

  /**
   * Default field separator: ': '
   */
  private static final Component DEF_FIELD_SEPARATOR = Component.text(": ");

  /**
   * New line pattern
   */
  public static final Pattern NEW_LINE_PATTERN = Pattern.compile("\n");

  /**
   * True, if the current line is empty, false otherwise
   */
  @Getter
  protected boolean lineEmpty = true;

  /**
   * The style a field has
   */
  @Getter
  @Setter
  protected Style fieldStyle = Style.empty();

  /**
   * The style a field's value has
   */
  @Getter
  @Setter
  protected Style fieldValueStyle = Style.empty();

  /**
   * The separator {@link #field(ComponentLike, ComponentLike)} uses to separate fields and values
   */
  @Getter
  @Setter
  protected Component fieldSeparator = DEF_FIELD_SEPARATOR;

  protected Audience viewer;

  /**
   * Empty constructor
   */
  public AbstractTextWriter() {
  }

  public void copyStyle(TextWriter other) {
    this.fieldSeparator = other.getFieldSeparator();
    this.fieldStyle = other.getFieldStyle();
    this.lineEmpty = other.isLineEmpty();
    this.fieldValueStyle = other.getFieldValueStyle();
  }

  @Override
  public @Nullable Audience viewer() {
    return viewer;
  }

  @Override
  public void viewer(@Nullable Audience audience) {
    this.viewer = audience;
  }

  @Override
  public @NotNull Component asComponent() {
    return null;
  }

  protected abstract void onNewLine();

  protected abstract void onClear();

  protected abstract void onWrite(Component text);

  /**
   * Writes the given text into this writer.
   * <p>
   * If the given text is a new line text, then it calls the {@link #onNewLine()} callback and moves
   * to the next line, else, it calls the {@link #onWrite(Component)} callback.
   *
   * @param text The text to write
   */
  @Override
  public void write(ComponentLike text) {
    Objects.requireNonNull(text, "Text was null");
    var component = text.asComponent();

    if (component.equals(Component.newline())) {
      newLine();
      return;
    }

    Iterator<Component> it = Text.split(NEW_LINE_PATTERN, component).iterator();

    while (it.hasNext()) {
      var next = it.next();
      onWrite(next);
      lineEmpty = false;

      if (it.hasNext()) {
        newLine();
      }
    }
  }

  /**
   * Clears the writer's written components
   */
  @Override
  public void clear() {
    lineEmpty = true;
    onClear();
  }


  /**
   * Moves the writer to the next line.
   * <p>
   * Implementations of the writer interface may rely on this method being called to properly format
   * the writer's results. One example of this is {@link BufferedTextWriter}
   */
  @Override
  public void newLine() {
    onNewLine();
    lineEmpty = true;
  }

  /**
   * Write an empty whitespace character
   */
  @Override
  public void space() {
    write(Component.space());
  }

  @Override
  public String getString() {
    return Text.toString(asComponent());
  }

  /**
   * Gets a plain text representation of what this writer has written so far
   *
   * @return Plain text result of this component
   */
  @Override
  public String getPlain() {
    return Text.plain(asComponent());
  }
}