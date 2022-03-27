package net.forthecrown.core.admin.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.core.admin.EntryNote;
import net.forthecrown.core.chat.ComponentWriter;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.inventory.FtcInventory;
import net.forthecrown.inventory.ItemStackBuilder;
import net.forthecrown.inventory.builder.ClickContext;
import net.forthecrown.inventory.builder.InventoryPos;
import net.forthecrown.inventory.builder.options.CordedInventoryOption;
import net.forthecrown.user.CrownUser;
import net.forthecrown.utils.ItemLoreBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import static net.forthecrown.core.chat.FtcFormatter.nonItalic;

record NoteOption(EntryNote note, InventoryPos pos) implements CordedInventoryOption {
    static final int MAX_CHARS_PER_LINE = 32;

    @Override
    public InventoryPos getPos() {
        return pos;
    }

    @Override
    public void place(FtcInventory inventory, CrownUser user) {
        ItemStackBuilder builder = new ItemStackBuilder(Material.MAP, 1);

        builder.setName(
                Component.text("Note by ")
                        .append(
                                Component.text(
                                        note.source(),
                                        NamedTextColor.YELLOW
                                )
                        )

                        .append(Component.text(", written: "))

                        .append(
                                FtcFormatter.formatDate(note.issued())
                                        .color(NamedTextColor.YELLOW)
                        )

                        .style(nonItalic())
        );

        String[] words = note.info().split(" ");
        ItemLoreBuilder loreBuilder = new ItemLoreBuilder();
        ComponentWriter writer = ComponentWriter.loreWriter(loreBuilder);

        int lineLength = 0;

        for (String s : words) {
            lineLength += s.length();

            writer.write(Component.text(s + " "));

            if (lineLength >= MAX_CHARS_PER_LINE) {
                writer.newLine();
                lineLength = 0;
            }
        }

        writer.newLine();
        builder.addLore(loreBuilder.getLore());
        inventory.setItem(getPos(), builder);
    }

    @Override
    public void onClick(CrownUser user, ClickContext context) throws CommandSyntaxException {}
}