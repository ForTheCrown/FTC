package net.forthecrown.utils;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import net.forthecrown.Loggers;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

public class BookBuilder {

  private static final Component NEW_LINE = Component.newline();

  private static final Style FILLER_STYLE = Style.style(NamedTextColor.WHITE);

  // https://minecraft.fandom.com/wiki/Book_and_Quill#Formatting_codes:~:text=No%20page%20may%20be%20longer%20than%2014%20lines%20and%20each%20line%20can%20have%20a%20width%20of%20114%20pixels
  public static final int PIXELS_PER_LINE = 114;
  public static final int MAX_LINES = 14;

  TextComponent.Builder currentPage = text();
  boolean pageAdded = true;
  boolean emptyPage = true;
  int pageCount = 0;
  int lineCount = 0;

  ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
  BookMeta bookMeta = (BookMeta) book.getItemMeta();

  public BookBuilder setTitle(String title) {
    bookMeta.setTitle(title);
    return this;
  }

  public BookBuilder setAuthor(String author) {
    bookMeta.setAuthor(author);
    return this;
  }

  public BookBuilder author(Component component) {
    bookMeta.author(component);
    return this;
  }

  public BookBuilder title(Component title) {
    bookMeta.title(title);
    return this;
  }

  public BookBuilder addEmptyLine() {
    return addText(Component.empty());
  }

  // Add text to current page
  public BookBuilder addText(Component line) {
    // Increase numLines according to new line length
    int extraLines = lineLength(line);

    Preconditions.checkState(extraLines <= MAX_LINES,
        "Given text has more lines than a single page, cannot add"
    );

    // If numLines too big, paste new line on next page
    if (lineCount + extraLines > MAX_LINES) {
      newPage();
    }

    lineCount += extraLines;

    if (!emptyPage) {
      currentPage.append(NEW_LINE);
    }

    currentPage.append(line);

    pageAdded = false;
    emptyPage = false;

    return this;
  }

  public BookBuilder addCentered(Component text) {
    String strText = Text.plain(text);
    int pxLength = TextInfo.getPxWidth(strText);
    int dif = PIXELS_PER_LINE - pxLength;

    Preconditions.checkArgument(dif >= 0, "Given text is longer than a single line");

    dif /= 2;

    return addText(
        text()
            .append(text(TextInfo.getFiller(dif), NamedTextColor.WHITE))
            .append(text)
            .build()
    );
  }

  public BookBuilder addField(Component field, Component value) {
    int hSize = TextInfo.getPxWidth(Text.plain(field));
    int oSize = TextInfo.getPxWidth(Text.plain(value));
    int fSize = PIXELS_PER_LINE - (oSize + hSize);

    Component filler;

    if (fSize > 0) {
      filler = text(TextInfo.getFiller(fSize), FILLER_STYLE);
    } else if (fSize == 0) {
      filler = Component.empty();
    } else {
      Loggers.getLogger().warn("Option too large for 1 line found: '{}'", Text.plain(field));
      filler = Component.empty();
    }

    return addText(Component.textOfChildren(field, filler, value));
  }

  public BookBuilder addFooter(Component footer) {
    if (lineLength(footer) > 1) {
      throw new IllegalStateException("Footer larger than a single line");
    }

    if (lineCount >= MAX_LINES) {
      throw new IllegalStateException("Page is already full");
    }

    while (lineCount < (MAX_LINES - 1)) {
      addEmptyLine();
    }

    return addText(footer);
  }

  public BookBuilder justifyRight(Component text) {
    if (lineLength(text) > 1) {
      throw new IllegalStateException("Footer larger than a single line");
    }

    int size = TextInfo.getPxWidth(Text.plain(text));

    if (size <= 0) {
      return this;
    }

    int fillerPixels = PIXELS_PER_LINE - size;

    return addText(
        text()
            .append(text(
                TextInfo.getFiller(fillerPixels), FILLER_STYLE
            ))

            .append(text)
            .build()

    );
  }

  public static int lineLength(Component line) {
    String text = Text.plain(line);
    String[] lines = text.split("\n");

    int lineCount = lines.length;

    for (var s : lines) {
      int length = TextInfo.getPxWidth(s);
      int extraLines = length / PIXELS_PER_LINE;

      lineCount += extraLines;
    }

    return lineCount;
  }

  public BookBuilder newPage() {
    bookMeta.addPages(currentPage.build());
    ++pageCount;
    pageAdded = true;
    emptyPage = true;

    currentPage = text(); // empty page
    lineCount = 0; // No lines yet
    return this;
  }

  public boolean canAddLine() {
    return canAddLines(1);
  }

  public boolean canAddLines(int lines) {
    int newLineCount = lineCount + lines;
    return newLineCount <= MAX_LINES;
  }

  public boolean hasTitle() {
    return bookMeta.hasTitle();
  }

  public boolean hasAuthor() {
    return bookMeta.hasAuthor();
  }

  @Nullable
  public String getAuthor() {
    return bookMeta.getAuthor();
  }

  public boolean hasGeneration() {
    return bookMeta.hasGeneration();
  }

  public @Nullable Generation getGeneration() {
    return bookMeta.getGeneration();
  }

  public void setGeneration(@Nullable Generation generation) {
    bookMeta.setGeneration(generation);
  }

  public boolean hasPages() {
    return bookMeta.hasPages();
  }

  @Nullable
  public Component author() {
    return bookMeta.author();
  }

  public BookMeta build() {
    if (!pageAdded) {
      newPage();
    }

    return bookMeta.clone();
  }
}