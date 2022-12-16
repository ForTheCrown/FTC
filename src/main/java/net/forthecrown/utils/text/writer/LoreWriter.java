package net.forthecrown.utils.text.writer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;
import java.util.stream.Collectors;

public class LoreWriter extends DefaultTextWriter {
    private List<Component> lore;
    private TextComponent.Builder line;

    public LoreWriter(List<Component> lore) {
        super(
                Component.text()
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        );
        this.lore = lore;
        line = createBuilder();
    }

    @Override
    protected void onNewLine() {
        super.onNewLine();

        lore.add(line.build());
        line = createBuilder();
    }

    @Override
    protected void onWrite(Component text) {
        super.onWrite(text);
        line.append(text);
    }

    @Override
    protected void onClear() {
        super.onClear();
        lore.clear();
        line = createBuilder();
    }

    public List<Component> getLore() {
        if (!lineEmpty) {
            lore.add(line.build());
            line = createBuilder();
        }

        return lore;
    }

    private TextComponent.Builder createBuilder() {
        var style = asComponent().style();

        return Component.text()
                .style(style)
                .colorIfAbsent(NamedTextColor.WHITE)
                .decoration(
                        TextDecoration.ITALIC, style.decoration(TextDecoration.ITALIC) == TextDecoration.State.TRUE
                                ? TextDecoration.State.TRUE : TextDecoration.State.FALSE
                );
    }

    @Override
    public void setColor(TextColor color) {
        super.setColor(color);

        lore = lore.stream()
                .map(component -> component.color(color))
                .collect(Collectors.toList());
    }

    @Override
    public void setStyle(Style style) {
        super.setStyle(style);

        lore = lore.stream()
                .map(component -> component.style(style))
                .collect(Collectors.toList());
    }
}