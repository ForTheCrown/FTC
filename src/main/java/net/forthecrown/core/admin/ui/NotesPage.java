package net.forthecrown.core.admin.ui;

import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriters;
import net.forthecrown.core.admin.EntryNote;
import net.forthecrown.core.admin.PunishEntry;
import net.forthecrown.user.User;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.inventory.menu.MenuNodeItem;
import net.forthecrown.utils.inventory.menu.context.InventoryContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static net.forthecrown.core.admin.ui.AdminUI.ENTRY;
import static net.forthecrown.utils.text.Text.nonItalic;

class NotesPage extends ListUiPage<EntryNote> {
    public NotesPage(AdminUiPage parent) {
        super(
                Component.text("Staff Notes"),
                parent
        );
    }

    @Override
    protected MenuNodeItem createMenuButton() {
        return (user, context) -> {
            var builder = ItemStacks.builder(Material.BOOK)
                    .setName("Staff notes");

            if (getList(context.get(ENTRY)).isEmpty()) {
                builder.addLore("&cNo notes to view!");
            } else {
                builder.addLore("&7Notes added by staff members");
            }

            return builder.build();
        };
    }

    @Override
    protected List<EntryNote> getList(PunishEntry entry) {
        return entry.getNotes();
    }

    @Override
    protected ItemStack getItem(EntryNote note, PunishEntry punishEntry) {
        var builder = ItemStacks.builder(Material.MAP)
                .setNameRaw(
                        Text.format("Note by: '{0}', written: {1, date}",
                                nonItalic(NamedTextColor.GRAY),
                                note.source(), note.issued()
                        )
                );

        String[] words = note.info().split(" ");
        var writer = TextWriters.loreWriter();

        int lineLength = 0;

        for (String s : words) {
            lineLength += s.length();

            writer.write(s + " ");

            // Try to limit the amount of character
            // on one line to just 20
            // if more than 20 characters on current
            // line, move to next line
            if (lineLength >= 20) {
                writer.newLine();
                lineLength = 0;
            }
        }

        writer.newLine();
        builder.setLore(writer.getLore());

        return builder.build();
    }

    @Override
    protected void onClick(EntryNote entry, int index, User user, InventoryContext context) {
    }
}