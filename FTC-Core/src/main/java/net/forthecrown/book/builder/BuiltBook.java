package net.forthecrown.book.builder;

import org.bukkit.inventory.ItemStack;


public class BuiltBook {

    private final ItemStack book;

    public BuiltBook(ItemStack bookItem) {
        book = bookItem;
    }

    public ItemStack getBookItem() { return this.book; }
}
