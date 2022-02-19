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
    int numLines = 0;

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


    // Add text to current page
    public BookBuilder addToPage (TextComponent line) {
        // 14 lines of text possible (0 -> 13)
        if (numLines == 13) {
            addPage();
            this.numLines = 0;
        }
        this.currentPage.append(line).append(NEW_LINE);
        ++this.numLines;
        return this;
    }

    public void addPage() {
        bookMeta.addPages(currentPage.build());
        currentPage = Component.text(); // empty page
        ++numPages;
    }


    @Override
    public BuiltBook build() {
        book.setItemMeta(bookMeta);
        return new BuiltBook(book);
    }

}
