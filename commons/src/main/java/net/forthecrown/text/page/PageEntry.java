package net.forthecrown.text.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.utils.context.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * The class which formats a single entry in a {@link PageFormat}. This is made up of 2 parts, an
 * index formatter and an entry formatter.
 * <p>
 * An {@link IndexFormatter} takes the viewer friendly index, and formats it into a text displayed
 * before the entry, normally this will just write an index with a ")" after it in yellow, but this
 * can be customized.
 * <p>
 * The other part is an {@link EntryDisplay} for formatting the entry itself. If this entry
 * formatter is null, then this formatter will attempt to use {@link Text#valueOf(Object)} to
 * produce a text
 *
 * @param <T> The type the entry formats
 * @see PageFormat
 * @see PagedIterator
 */
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "create")
@Accessors(chain = true)
public class PageEntry<T> {

  /**
   * The function that takes a viewer index and formats it, by default just writes the index with a
   * ")" appended to it, in yellow
   */
  private IndexFormatter<T> index = IndexFormatter.DEFAULT;

  /**
   * Individual entry formatter
   */
  private EntryDisplay<T> entryDisplay;

  /**
   * Creates a page entry with the given entry display
   *
   * @param display The formatter to use
   * @param <T>     The entry's type
   * @return The created entry formatter
   */
  public static <T> PageEntry<T> of(EntryDisplay<T> display) {
    return PageEntry.<T>create().setEntryDisplay(display);
  }

  public void write(PagedIterator<T> it, T entry,
                    TextWriter writer,
                    Context context
  ) {
    Component indexText = index.createIndex(it.getViewerIndex(), entry, it);

    if (indexText != null) {
      writer.write(indexText);
      writer.space();
    }

    if (entryDisplay == null) {
      writer.write(Text.valueOf(entry));
    } else {
      entryDisplay.write(writer, entry, it.getViewerIndex(), context, it);
    }
  }

  @FunctionalInterface
  public interface EntryDisplay<T> {
    void write(TextWriter writer,
               T entry,
               int viewerIndex,
               Context context,
               PagedIterator<T> it
    );
  }

  @FunctionalInterface
  public interface IndexFormatter<T> {
    IndexFormatter DEFAULT = (viewerIndex, entry1, it) -> {
      return Component.text(viewerIndex + ")", NamedTextColor.YELLOW);
    };

    Component createIndex(int viewerIndex, T entry, PagedIterator<T> it);
  }
}