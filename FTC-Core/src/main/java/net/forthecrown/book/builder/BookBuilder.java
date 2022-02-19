package net.forthecrown.book.builder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.builder.Builder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookBuilder implements Builder<BuiltBook> {
    Component NEW_LINE = Component.newline();

    String title;
    String author;
    TextComponent.Builder currentPage = Component.text();
    int numPages = 0;
    int numLinesOnCurrentPage = 0;

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


    public BookBuilder setNumPages(int numPages) {
        this.numPages = numPages;
        return this;
    }

    public BookBuilder addEmptyLine() {
        if (numLinesOnCurrentPage >= 13) {
            addPage();
            this.numLinesOnCurrentPage = 0;
        }
        ++this.numLinesOnCurrentPage;
        this.currentPage.append(NEW_LINE);
        return this;
    }

    // Add text to current page
    public BookBuilder addText(TextComponent line) {
        if (line.content().length() == 0) return this; // Don't add empty lines
        if (line.content().length() >= 14 * 114) return this; // Don't add oversized lines

        int futureAmountOfLines = numLinesOnCurrentPage;

        // Increase numLines according to new line length
        int lineLength = TextInfo.getPxLength(line.content());
        while (lineLength > 0) {
            ++futureAmountOfLines;
            lineLength -= 114;
        }

        // If numLines too big, paste new line on next page
        // 14 lines of text possible (0 -> 13)
        if (futureAmountOfLines > 13) addPage();

        this.currentPage.append(line).append(NEW_LINE);
        numLinesOnCurrentPage += (futureAmountOfLines - numLinesOnCurrentPage);
        return this;
    }

    public void addPage() {
        bookMeta.addPages(currentPage.build());
        ++numPages;

        currentPage = Component.text(); // empty page
        numLinesOnCurrentPage = 0; // No lines yet
    }

    public BookBuilder newPage() {
        addPage();
        return this;
    }

    public BookBuilder addAmountOfLines() {
        int currentAmountOfLines = numLinesOnCurrentPage;
        addText(Component.text("#Lines: " + currentAmountOfLines));
        return this;
    }


    @Override
    public BuiltBook build() {
        addPage();
        book.setItemMeta(bookMeta);
        return new BuiltBook(book);
    }

}
