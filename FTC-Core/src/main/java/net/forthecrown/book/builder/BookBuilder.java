package net.forthecrown.book.builder;

import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;

public class BookBuilder implements Builder<BuiltBook> {
    private static final Component NEW_LINE = Component.newline();

    public static final int PIXELS_PER_LINE = 114;
    public static final int MAX_LINES = 14;

    TextComponent.Builder currentPage = Component.text();
    boolean pageAdded;
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
        line = ChatUtils.renderToSimple(line);
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

        this.currentPage.append(line).append(NEW_LINE);
        pageAdded = false;
        return this;
    }

    public BookBuilder addCentered(Component text) {
        text = ChatUtils.renderToSimple(text);
        String strText = ChatUtils.plainText(text);
        int pxLength = TextInfo.getPxLength(strText);
        int dif = PIXELS_PER_LINE - pxLength;

        Validate.isTrue(dif >= 0, "Given text is longer than a single line");

        dif /= 4;

        return addText(
                Component.text()
                        .append(Component.text(".".repeat(dif)))
                        .append(text)
                        .build()
        );
    }

    public int lineLength(Component line) {
        line = ChatUtils.renderToSimple(line);

        String text = ChatUtils.plainText(line);
        int pxLength = TextInfo.getPxLength(text);

        return (int) Math.ceil((double) pxLength / (double) PIXELS_PER_LINE);
    }

    private void addPage() {
        bookMeta.addPages(currentPage.build());
        ++pageCount;
        pageAdded = true;

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

    public BookMeta.@Nullable Generation getGeneration() {
        return bookMeta.getGeneration();
    }

    public void setGeneration(BookMeta.@Nullable Generation generation) {
        bookMeta.setGeneration(generation);
    }

    public boolean hasPages() {
        return bookMeta.hasPages();
    }

    @Nullable
    public Component author() {
        return bookMeta.author();
    }

    public Book buildBook() {
        if(!pageAdded) addPage();
        return bookMeta.clone();
    }

    @Override
    public BuiltBook build() {
        if(!pageAdded) addPage();
        book.setItemMeta(bookMeta);
        return new BuiltBook(book);
    }

}
