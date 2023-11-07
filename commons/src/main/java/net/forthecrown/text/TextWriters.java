package net.forthecrown.text;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

/**
 * Factory class for creating {@link AbstractTextWriter} instances.
 */
public final class TextWriters {
  private TextWriters() {}

  /**
   * Creates a standard text writer
   *
   * @return The created writer
   */
  public static DefaultTextWriter newWriter() {
    return new DefaultTextWriter(Component.text());
  }

  /**
   * Wraps a given builder for a writer
   *
   * @param builder The builder to wrap
   * @return The created writer
   */
  public static DefaultTextWriter wrap(TextComponent.Builder builder) {
    return new DefaultTextWriter(builder);
  }

  /**
   * Creates a text writer which writes to a string.
   *
   * @return The created writer
   */
  public static AbstractTextWriter stringWriter() {
    return new StringTextWriter();
  }

  /**
   * Creates a writer which writes into a line-based list
   *
   * @param lines The text lines to use as a backing list.
   * @return The created writer
   */
  public static BufferedTextWriter buffered(List<Component> lines) {
    return new BufferedTextWriter(lines);
  }

  /**
   * Creates a text writer that writes to an empty {@link List} of components.
   *
   * @return The created writer
   */
  public static BufferedTextWriter buffered() {
    return buffered(new ObjectArrayList<>());
  }
}