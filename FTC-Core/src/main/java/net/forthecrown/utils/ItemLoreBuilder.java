package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.chat.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * A small and quick utility class to help to add lore
 * to an item.
 */
public class ItemLoreBuilder {
    private final List<Component> lore;

    public ItemLoreBuilder() {
        this(new ObjectArrayList<>());
    }

    public ItemLoreBuilder(List<Component> lore) {
        this.lore = lore;
    }

    public ItemLoreBuilder addAll(Component... lore) {
        for (Component c: Validate.noNullElements(lore)) {
            add(c);
        }

        return this;
    }

    public ItemLoreBuilder addAll(String... lore) {
        for (String s: Validate.noNullElements(lore)) {
            add(s);
        }

        return this;
    }

    public ItemLoreBuilder addAll(Iterable<Component> lore) {
        for (Component c: Validate.notNull(lore)) {
            add(c);
        }

        return this;
    }

    public ItemLoreBuilder addAllStrings(Iterable<String> lore) {
        for (String s: Validate.notNull(lore)) {
            add(s);
        }

        return this;
    }

    public ItemLoreBuilder add(Component c) {
        lore.add(ChatUtils.renderToSimple(c));
        return this;
    }

    public ItemLoreBuilder add(String s) {
        return add(ChatUtils.stringToNonItalic(s, true));
    }

    public ItemLoreBuilder addEmpty() {
        return add(Component.empty());
    }

    public ItemLoreBuilder addAll(Style style, Component... arr) {
        for (Component c: Validate.noNullElements(arr)) {
            add(c.style(style));
        }

        return this;
    }

    public List<Component> getLore() {
        return lore;
    }

    public Prefixed withIndent(int indent) {
        Validate.isTrue(indent > 0, "indent level cannot be less than 0");
        return withPrefixedLines(" ".repeat(indent));
    }

    public Prefixed withPrefixedLines(String prefix) {
        return new Prefixed(prefix, this);
    }

    public static class Prefixed extends ItemLoreBuilder {
        private final String prefix;
        private final ItemLoreBuilder builder;

        public Prefixed(String prefix, ItemLoreBuilder builder) {
            this.prefix = prefix;
            this.builder = builder;
        }

        public Component indentText() {
            return Component.text(prefix);
        }

        public ItemLoreBuilder getBuilder() {
            return builder;
        }

        public String getPrefix() {
            return prefix;
        }

        @Override
        public ItemLoreBuilder add(Component c) {
            return builder.add(indentText().append(c));
        }

        @Override
        public List<Component> getLore() {
            return builder.lore;
        }
    }
}