package net.forthecrown.text.page;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.utils.context.Context;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Nullable;

/**
 * A formatter used to format {@link PagedIterator} page footers.
 * <p>
 * This formatter specifically only deals with the part of a page that comes after the entries, for
 * example, this would format the border, page buttons and page numbers that are at the bottom of a
 * text displayed by a command like <code>/baltop</code>
 *
 * @see PageFormat
 * @see PagedIterator
 */
@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "create")
public class Footer implements PageElement {

  public static final Component GENERIC_BORDER
      = text("                  ", NamedTextColor.GRAY, TextDecoration.STRIKETHROUGH);

  /**
   * Standard " < " previous page button with hover text, and bold and yellow styling
   */
  public static final TextComponent PREVIOUS_PAGE
      = text(" < ", NamedTextColor.YELLOW, TextDecoration.BOLD)
      .hoverEvent(translatable("spectatorMenu.previous_page"));

  /**
   * Standard " > " next page button with hover text, and bold and yellow styling
   */
  public static final TextComponent NEXT_PAGE
      = text(" > ", NamedTextColor.YELLOW, TextDecoration.BOLD)
      .hoverEvent(translatable("spectatorMenu.next_page"));

  /**
   * The border displayed before and after the page buttons and numbers
   */
  private Component border = GENERIC_BORDER;

  /**
   * The page button click event provider
   */
  private PageButtonProvider pageButton;

  /**
   * The color to use for the footer, by default, yellow
   */
  private TextColor color = NamedTextColor.YELLOW;

  /**
   * Creates footer with the given command format.
   *
   * @param commandFormat The command format to use
   * @return The created footer
   * @see #setPageButton(String)
   */
  public static Footer ofButton(String commandFormat) {
    return create().setPageButton(commandFormat);
  }

  @Override
  public void write(PagedIterator it, TextWriter writer, Context context) {
    if (border != null) {
      writer.write(border);
    }

    // Write previous page button only
    // if there is a previous page to go to
    if (pageButton != null && !it.isFirstPage()) {
      writer.write(
          previousPage(pageButton.create(it.getPage(), it.getPageSize(), context)).color(color)
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
          nextPage(pageButton.create(it.getPage() + 2, it.getPageSize(), context)).color(color)
      );
    } else if (border != null) {
      writer.space();
    }

    if (border != null) {
      writer.write(border);
    }
  }

  /**
   * Creates a next page button by applying the given click event to the {@link #NEXT_PAGE}
   * constant.
   *
   * @param event The click event to apply, may be null
   * @return The created text
   */
  static Component nextPage(@Nullable ClickEvent event) {
    return NEXT_PAGE.clickEvent(event);
  }

  /**
   * Creates a previous page button by applying the given click event to the {@link #PREVIOUS_PAGE}
   * constant.
   *
   * @param event The click event to apply, may be null
   * @return The created text
   */
  static Component previousPage(@Nullable ClickEvent event) {
    return PREVIOUS_PAGE.clickEvent(event);
  }

  /**
   * Sets the border text displayed before and after the page buttons and page numbers.
   *
   * @param border The border to use
   * @return This
   */
  public Footer setBorder(Component border) {
    this.border = border;
    return this;
  }

  /**
   * Sets the page button click event provider that's used to provide a click event to the "<" and
   * ">" buttons
   *
   * @param pageButton The page button provider
   * @return This
   */
  public Footer setPageButton(PageButtonProvider pageButton) {
    this.pageButton = pageButton;
    return this;
  }

  /**
   * Sets the click event to use for changing pages.
   * <p>
   * This uses {@link String#format(String, Object...)} to apply 2 arguments to the given format,
   * these
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
        (viewerPage, pageSize, context) -> {
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
   *
   * @param color The color to use
   * @return This
   */
  public Footer setColor(TextColor color) {
    this.color = color;
    return this;
  }

  public interface PageButtonProvider {
    ClickEvent create(int viewerPage, int pageSize, Context context);
  }
}