package net.forthecrown.text.format.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.forthecrown.text.Messages;
import net.forthecrown.text.Text;
import net.forthecrown.text.writer.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * A formatter used to format {@link PageEntryIterator}
 * page footers.
 * <p>
 * This formatter specifically only deals with the part
 * of a page that comes after the entries, for example,
 * this would format the border, page buttons and page
 * numbers that are at the bottom of a text displayed
 * by a command like <code>/baltop</code>
 * @see PageFormat
 * @see PageEntryIterator
 */
@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "create")
public class Footer implements PageElement {
    /** The border displayed before and after the page buttons and numbers */
    private Component border = Messages.PAGE_BORDER;

    /** The page button click event provider */
    private PageButtonProvider pageButton;

    /** The color to use for the footer, by default, yellow */
    private TextColor color = NamedTextColor.YELLOW;

    /**
     * Creates footer with the given command format.
     * @param commandFormat The command format to use
     * @return The created footer
     * @see #setPageButton(String)
     */
    public static Footer ofButton(String commandFormat) {
        return create().setPageButton(commandFormat);
    }

    @Override
    public void write(PageEntryIterator it, TextWriter writer) {
        if (border != null) {
            writer.write(border);
        }

        // Write previous page button only
        // if there is a previous page to go to
        if (pageButton != null && !it.isFirstPage()) {
            writer.write(
                    Messages.previousPage(pageButton.create(it.getPage(), it.getPageSize()))
                            .color(color)
            );
        } else if (border != null) {
            writer.space();
        }

        // Write page numbers
        writer.write(
                Text.format("Page {0}/{1}",
                        color,
                        it.getPage() + 1,
                        it.getMaxPage()
                )
        );

        // Write next page button only if there's
        // a next page to go to
        if (pageButton != null && !it.isLastPage()) {
            writer.write(
                    Messages.nextPage(pageButton.create(it.getPage() + 2, it.getPageSize()))
                            .color(color)
            );
        } else if (border != null) {
            writer.space();
        }

        if (border != null) {
            writer.write(border);
        }
    }

    /**
     * Sets the border text displayed before
     * and after the page buttons and page
     * numbers.
     * @param border The border to use
     * @return This
     */
    public Footer setBorder(Component border) {
        this.border = border;
        return this;
    }

    /**
     * Sets the page button click event provider
     * that's used to provide a click event to the
     * "<" and ">" buttons
     * @param pageButton The page button provider
     * @return This
     */
    public Footer setPageButton(PageButtonProvider pageButton) {
        this.pageButton = pageButton;
        return this;
    }

    /**
     * Sets the click event to use for
     * changing pages.
     * <p>
     * This uses {@link String#format(String, Object...)}
     * to apply 2 arguments to the given format, these
     * arguments are: <pre>
     *  1. The page number displayed to the viewer
     *  2. The size of the page, the amount of entries
     *     displayed on 1 page
     * </pre>
     *
     * @param format The format to use
     * @return This
     */
    public Footer setPageButton(String format) {
        return setPageButton(
                (viewerPage, pageSize) -> {
                    return ClickEvent.runCommand(
                            String.format(
                                    format,
                                    viewerPage, pageSize
                            )
                    );
                }
        );
    }

    /**
     * Sets the base color used to format the footer
     * @param color The color to use
     * @return This
     */
    public Footer setColor(TextColor color) {
        this.color = color;
        return this;
    }

    public interface PageButtonProvider {
        ClickEvent create(int viewerPage, int pageSize);
    }
}