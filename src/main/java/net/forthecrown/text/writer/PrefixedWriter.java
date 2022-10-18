package net.forthecrown.text.writer;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

@Getter
public class PrefixedWriter extends TextWriter {
    private final TextWriter base;
    private final Component prefix;

    public PrefixedWriter(TextWriter other, Component prefix) {
        super(other);
        this.base = other;
        this.prefix = prefix;
    }

    @Override
    protected void onWrite(Component text) {
        if (lineEmpty) {
            base.write(prefix);
        }

        base.write(text);
    }

    @Override
    protected void onNewLine() {
        base.newLine();
    }

    @Override
    protected void onClear() {
        base.clear();
    }

    @Override
    public @NotNull Component asComponent() {
        return base.asComponent();
    }

    @Override
    public boolean isLineEmpty() {
        return getBase().isLineEmpty();
    }

    @Override
    public Style getFieldStyle() {
        return getBase().getFieldStyle();
    }

    @Override
    public Component getFieldSeparator() {
        return getBase().getFieldSeparator();
    }

    @Override
    public String getString() {
        return getBase().getString();
    }

    @Override
    public String getPlain() {
        return getBase().getPlain();
    }

    @Override
    public Style getFieldValueStyle() {
        return getBase().getFieldValueStyle();
    }

    @Override
    public void setFieldStyle(Style fieldStyle) {
        getBase().setFieldStyle(fieldStyle);
    }

    @Override
    public void setFieldValueStyle(Style fieldValueStyle) {
        getBase().setFieldValueStyle(fieldValueStyle);
    }

    @Override
    public void setFieldSeparator(Component fieldSeparator) {
        getBase().setFieldSeparator(fieldSeparator);
    }
}