package net.forthecrown.core.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;

import java.util.Objects;

public interface FieldedWriter extends ComponentWriter {
    default void writeField(String field, Component value) {
        newLine();
        write(
                Component.text(field + ": ")
                        .style(fieldStyle())
        );
        write(value);
    }

    default void writeField(String field, Object value, boolean formatColors) {
        if(value == null) return;
        writeField(field, ChatUtils.convertString(Objects.toString(value.toString()), formatColors));
    }


    Style fieldStyle();
    void fieldStyle(Style style);

    static FieldedWriter create() {
        return new FieldedWriter() {
            TextComponent.Builder builder = Component.text();
            Style style = Style.empty();

            @Override
            public Style fieldStyle() {
                return style;
            }

            @Override
            public void fieldStyle(Style style) {
                this.style = style;
            }

            @Override
            public void write(Component text) {
                builder.append(text);
            }

            @Override
            public void clear() {
                builder = Component.text();
            }

            @Override
            public Component get() {
                return builder.build();
            }
        };
    }

    static FieldedWriter wrap(ComponentWriter writer) {
        return new FieldedWriter() {
            Style style = Style.empty();

            @Override
            public Style fieldStyle() {
                return style;
            }

            @Override
            public void fieldStyle(Style style) {
                this.style = style;
            }

            @Override
            public void write(Component text) {
                writer.write(text);
            }

            @Override
            public void newLine() {
                writer.newLine();
            }

            @Override
            public void clear() {
                writer.clear();
            }

            @Override
            public Component get() {
                return writer.get();
            }
        };
    }
}