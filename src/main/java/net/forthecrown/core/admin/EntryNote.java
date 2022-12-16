package net.forthecrown.core.admin;

import com.google.gson.JsonElement;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.utils.JsonSerializable;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

/**
 * A single staff note
 */
public record EntryNote(String info, long issued, String source) implements JsonSerializable {
    public static final String
            KEY_INFO = "content",
            KEY_ISSUED = "issued",
            KEY_SOURCE = "source";

    @Override
    public JsonElement serialize() {
        JsonWrapper json = JsonWrapper.create();

        json.add(KEY_INFO, info);
        json.addTimeStamp(KEY_ISSUED, issued);
        json.add(KEY_SOURCE, source);

        return json.getSource();
    }

    public static EntryNote read(JsonElement element) {
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        return new EntryNote(
                json.getString(KEY_INFO),
                json.getTimeStamp(KEY_ISSUED),
                json.getString(KEY_SOURCE)
        );
    }

    public static EntryNote of(String info, CommandSource issuer) {
        return new EntryNote(
                info,
                System.currentTimeMillis(),
                issuer.textName()
        );
    }

    public static void writeNotes(List<EntryNote> notes, TextWriter writer, User noteHolder) {
        writer.formatted("{0, user}'s staff notes:", noteHolder);

        for (int i = 0; i < notes.size(); i++) {
            var entry = notes.get(i);
            var viewIndex = i + 1;

            writer.newLine();
            writer.write(
                    Text.format("{0}) ", NamedTextColor.YELLOW, viewIndex)
                            .hoverEvent(Component.text("Click to remove"))
                            .clickEvent(ClickEvent.runCommand("/notes " + noteHolder.getName() + " remove " + viewIndex))
            );

            writer.write(
                    Component.text(entry.info)
                            .hoverEvent(
                                    Text.format("Source: {0}\nCreated: {1, date}",
                                            entry.source, entry.issued
                                    )
                            )
            );
        }
    }
}