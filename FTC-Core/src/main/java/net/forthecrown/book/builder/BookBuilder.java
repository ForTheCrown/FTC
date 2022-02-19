package net.forthecrown.book.builder;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookBuilder implements Builder<BuiltBook> {
    private static final Component NEW_LINE = Component.newline();

    public static final int PIXELS_PER_LINE = 114;
    public static final int MAX_LINES = 14;

    String title;
    String author;
    TextComponent.Builder currentPage = Component.text();
    int pageCount = 0;
    int lineCount = 0;

    ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
    BookMeta bookMeta = (BookMeta) book.getItemMeta();

    public BookBuilder setTitle(String title) {
        this.title = title;
        bookMeta.setTitle(title);
        return this;
    }

    public BookBuilder setAuthor(String author) {
        this.author = author;
        bookMeta.setAuthor(author);
        return this;
    }


    public BookBuilder setPageCount(int pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    public BookBuilder addEmptyLine() {
        return addText(Component.empty());
    }

    // Add text to current page
    public BookBuilder addText(Component line) {
        // TextComponent.content() only returns the direct content of a text component
        // children are not included, this turns the entire component into a plain string,
        // so stuff like color codes don't interfere
        String text = ChatUtils.plainText(line);
        int pxLength = TextInfo.getPxLength(text);

        // Ensure the text isn't larger than a single page
        Validate.isTrue(pxLength < PIXELS_PER_LINE * MAX_LINES, "Text too big :(");

        // Increase numLines according to new line length
        // I love integer division
        int extraLines = (int) Math.ceil((double) pxLength / (double) PIXELS_PER_LINE);

        // If numLines too big, paste new line on next page
        // 14 lines of text possible (0 -> 13)
        if (lineCount + extraLines > MAX_LINES) addPage();

        lineCount += extraLines;

        this.currentPage.append(ChatUtils.renderToSimple(line)).append(NEW_LINE);
        return this;
    }

    public void addPage() {
        bookMeta.addPages(currentPage.build());
        ++pageCount;

        currentPage = Component.text(); // empty page
        lineCount = 0; // No lines yet
    }

    public BookBuilder newPage() {
        addPage();
        return this;
    }

    public BookBuilder addAmountOfLines() {
        addText(Component.text("#Lines: " + lineCount));
        return this;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getPageCount() {
        return pageCount;
    }

    @Override
    public BuiltBook build() {
        addPage();
        book.setItemMeta(bookMeta);
        return new BuiltBook(book);
    }

}
