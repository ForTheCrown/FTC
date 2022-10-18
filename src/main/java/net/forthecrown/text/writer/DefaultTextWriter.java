package net.forthecrown.text.writer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public class DefaultTextWriter extends TextWriter {
    private TextComponent.Builder builder;

    public DefaultTextWriter(TextComponent.Builder builder) {
        this.builder = builder;
    }

    @Override
    protected void onNewLine() {
        builder.append(Component.newline());
    }

    @Override
    protected void onClear() {
        var style = builder.build().style();

        builder = Component.text()
                .style(style);
    }

    @Override
    protected void onWrite(Component text) {
        builder.append(text);
    }

    @Override
    public @NotNull Component asComponent() {
        return builder.build();
    }

    public void setStyle(Style style) {
        builder.style(style);
    }

    public void setColor(TextColor color) {
        builder.color(color);
    }
}