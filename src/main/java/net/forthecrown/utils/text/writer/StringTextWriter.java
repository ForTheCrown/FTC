package net.forthecrown.utils.text.writer;

import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.NotNull;

import static org.apache.commons.lang3.CharUtils.LF;

public class StringTextWriter extends TextWriter {
    private final StringBuilder builder = new StringBuilder();
    private final StringBuilder plainBuilder = new StringBuilder();
    private TextComponent.Builder textBuilder = Component.text();

    @Override
    public void onWrite(Component text) {
        builder.append(Text.toString(text));
        plainBuilder.append(Text.plain(text));

        textBuilder.append(text);
    }

    @Override
    protected void onNewLine() {
        builder.append(LF);
        plainBuilder.append(LF);

        textBuilder.append(Component.newline());
    }

    @Override
    public void onClear() {
        builder.delete(0, builder.length());
        plainBuilder.delete(0, plainBuilder.length());
        textBuilder = Component.text();
    }

    @Override
    public String getPlain() {
        return plainBuilder.toString();
    }

    @Override
    public String getString() {
        return builder.toString();
    }

    @Override
    public @NotNull Component asComponent() {
        return textBuilder.build();
    }
}