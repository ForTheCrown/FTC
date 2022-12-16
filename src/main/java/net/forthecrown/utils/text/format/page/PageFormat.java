package net.forthecrown.utils.text.format.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;

/**
 * A text formatter used to format a given {@link PageEntryIterator}
 * into pages with a header, footer and body.
 * <p>
 * All the leg work of this paginator is actually done by the three
 * following classes:
 * <p>
 * {@link Header} for formatting a list's header, {@link PageEntry}
 * for formatting each individual entry on a page, {@link Footer}
 * for formatting a page's footer, aka the page number and buttons
 * on the bottom.
 * <p>
 * In a way, this class just acts as a delegate for it's 3 components
 * and ensures that the header and each entry in the page, and footer
 * are on different lines
 * @see Header
 * @see Footer
 * @see PageEntry
 * @see PageEntryIterator
 * @param <T> The type this pagination formats
 */
@Getter @Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "create")
@Accessors(chain = true)
public class PageFormat<T> {
    /** The header formatter */
    private Header<T> header;

    /** The footer formatter */
    private Footer footer;

    /** Entry formatter */
    private PageEntry<T> entry = PageEntry.create();

    /**
     * Formats the given page iterator into a displayable
     * page
     * @param it The iterator to format
     * @return The formatted message
     * @see #write(PageEntryIterator, TextWriter)
     */
    public Component format(PageEntryIterator<T> it) {
        var writer = TextWriters.newWriter();
        write(it, writer);

        return writer.asComponent();
    }

    /**
     * Writes the given iterator into the given
     * text writer.
     * <p>
     * If the given iterator has no entries, then
     * will just write the header and footer right
     * after each other. If both the iterator have
     * no entries and the header and footer are null,
     * this writes nothing
     * @param it The iterator to write and format
     * @param writer The destination of the component writing
     */
    public void write(PageEntryIterator<T> it, TextWriter writer) {
        // Header might be null so also make sure
        // to not write the new line, otherwise you're gonna have
        // an empty line lol
        if (header != null) {
            header.write(it, writer);
            writer.newLine();
        }

        while (it.hasNext()) {
            var t = it.next();

            entry.write(it, t, writer);

            if (it.hasNext()) {
                writer.newLine();
            }
        }

        // Same logic as above with the header, only
        // write the newline if we have a footer, otherwise
        // there'll be a blank line lol
        if (footer != null) {
            writer.newLine();
            footer.write(it, writer);
        }
    }

    /**
     * Sets the header of this format to
     * have the given title, separated by {@link Messages#PAGE_BORDER}
     * borders
     * @param title The title to use
     * @return This
     * @see Header#of(Component)
     * @see #setHeader(Header)
     */
    public PageFormat<T> setHeader(Component title) {
        return setHeader(Header.of(title));
    }

    /**
     * Sets the page header formatter
     * @param header header formatter
     * @return This
     * @see Header
     */
    public PageFormat<T> setHeader(Header<T> header) {
        this.header = header;
        return this;
    }

    /**
     * Sets the command format to use for
     * the footer's page button provider
     * <p>
     * Please see {@link Footer#setPageButton(String)}
     * for details about the format itself.
     * @param commandFormat The format to use
     * @return This
     * @see Footer#setPageButton(String)
     * @see Footer#ofButton(String)
     * @see #setFooter(Footer)
     */
    public PageFormat<T> setPageButton(String commandFormat) {
        return setFooter(Footer.ofButton(commandFormat));
    }

    /**
     * Sets the entry formatter to use
     * @param entry The entry formatter
     * @return This
     * @see PageEntry
     */
    public PageFormat<T> setEntry(PageEntry<T> entry) {
        this.entry = entry;
        return this;
    }

    /**
     * Sets the entry formatter to use.
     * <p>
     * Creates an entry formatter using {@link PageEntry#of(PageEntry.EntryDisplay)}
     * with the default index formatter
     *
     * @param entryFormatter The entry formatter to use
     * @return This
     * @see #setEntry(PageEntry)
     */
    public PageFormat<T> setEntry(PageEntry.EntryDisplay<T> entryFormatter) {
        return setEntry(PageEntry.of(entryFormatter));
    }
}