package net.forthecrown.text.page;

import net.forthecrown.text.TextWriter;
import net.forthecrown.utils.context.Context;

/**
 * An element which can be written by {@link PageFormat} to display an element of a page
 *
 * @param <T> The type this element formats
 */
@FunctionalInterface
public interface PageElement<T> {

  /**
   * Writes this element's formatted data to the given writer using the given iterator
   *
   * @param it     The page being iterated through
   * @param writer The destination of the text
   */
  void write(PageEntryIterator<T> it, TextWriter writer, Context context);
}