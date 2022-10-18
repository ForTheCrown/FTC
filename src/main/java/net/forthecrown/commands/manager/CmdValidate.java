package net.forthecrown.commands.manager;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.text.format.page.PageEntryIterator;

public final class CmdValidate {
    private CmdValidate() {}

    public static void index(int index, int size) throws CommandSyntaxException {
        if (index > size) {
            throw Exceptions.invalidIndex(index, size);
        }
    }

    public static void page(int page, int pageSize, int size) throws CommandSyntaxException {
        var max = PageEntryIterator.getMaxPage(pageSize, size);

        if (page >= max) {
            throw Exceptions.invalidPage(page, max);
        }
    }
}