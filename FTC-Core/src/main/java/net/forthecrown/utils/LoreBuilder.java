package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.Validate;

import java.util.List;

public class LoreBuilder {
    private final List<Component> lore;

    public LoreBuilder() {
        this(new ObjectArrayList<>());
    }

    public LoreBuilder(List<Component> lore) {
        this.lore = lore;
    }

    public LoreBuilder addAll(Component... lore) {
        for (Component c: Validate.noNullElements(lore)) {
            add(c);
        }

        return this;
    }

    public LoreBuilder addAll(String... lore) {
        for (String s: Validate.noNullElements(lore)) {
            add(s);
        }

        return this;
    }

    public LoreBuilder addAll(Iterable<Component> lore) {
        for (Component c: Validate.notNull(lore)) {
            add(c);
        }

        return this;
    }

    public LoreBuilder addAllStrings(Iterable<String> lore) {
        for (String s: Validate.notNull(lore)) {
            add(s);
        }

        return this;
    }

    public LoreBuilder add(Component c) {
        lore.add(ChatUtils.renderIfTranslatable(c));
        return this;
    }

    public LoreBuilder add(String s) {
        return add(
                Component.text()
                        .decoration(TextDecoration.ITALIC, false)
                        .append(ChatUtils.stringToNonItalic(s, true))
                        .build()
        );
    }

    public LoreBuilder addEmpty() {
        return add(Component.empty());
    }

    public LoreBuilder addAll(Style style, Component... arr) {
        for (Component c: Validate.noNullElements(arr)) {
            add(c.style(style));
        }

        return this;
    }

    public List<Component> getLore() {
        return lore;
    }
}
