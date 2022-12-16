package net.forthecrown.core.config;

import lombok.experimental.UtilityClass;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;

import java.util.Objects;

@ConfigData(filePath = "joininfo.json")
public @UtilityClass class JoinInfo {
    public Info endInfo = new Info();
    public Info info = new Info();
    public Info endWeek = new Info();

    public Component display() {
        TextWriter writer = TextWriters.newWriter();

        info.write(writer);
        endInfo.write(writer);
        endWeek.write(writer);

        return writer.asComponent();
    }

    public class Info {
        public Component text = Component.empty();
        public boolean visible = false;

        void write(TextWriter writer) {
            if (!visible
                    || text == null
                    || Objects.equals(text, Component.empty())
            ) {
                return;
            }

            writer.line(text);
        }
    }
}