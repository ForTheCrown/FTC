package net.forthecrown.core.config;

import lombok.experimental.UtilityClass;
import net.forthecrown.core.Messages;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.forthecrown.utils.ArrayIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;

@ConfigData(filePath = "rules.json")
public @UtilityClass class ServerRules {
    // Use array, because that's safer for GSON to use
    public Component[] rules = new Component[0];

    public ComponentLike display() {
        TextWriter writer = TextWriters.newWriter();
        writer.write(Messages.PAGE_BORDER);
        writer.write(" Server rules ");
        writer.write(Messages.PAGE_BORDER);

        var it = ArrayIterator.unmodifiable(rules);

        while (it.hasNext()) {
            var c = it.next();
            int index = it.nextIndex();

            writer.line(index + ") ", NamedTextColor.GRAY);
            writer.write(c);
        }

        return writer;
    }
}