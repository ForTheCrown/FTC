package net.forthecrown.utils.text.writer;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.List;

/**
 * Factory class for creating {@link TextWriter} instances.
 */
public final class TextWriters {
    private TextWriters() {}

    /**
     * Creates a standard text writer
     * @return The created writer
     */
    public static TextWriter newWriter() {
        return new DefaultTextWriter(Component.text());
    }

    /**
     * Wraps a given builder for a writer
     * @param builder The builder to wrap
     * @return The created writer
     */
    public static TextWriter wrap(TextComponent.Builder builder) {
        return new DefaultTextWriter(builder);
    }

    /**
     * Creates a text writer which writes to a string.
     * @return The created writer
     */
    public static TextWriter stringWriter() {
        return new StringTextWriter();
    }

    /**
     * Creates a writer which writes into item lore
     * @param lore The item lore to use as a backing lore.
     * @return The created writer
     */
    public static LoreWriter loreWriter(List<Component> lore) {
        return new LoreWriter(lore);
    }

    /**
     * Creates a text writer that writes to an empty
     * {@link List} of components.
     * @return The created writer
     */
    public static LoreWriter loreWriter() {
        return loreWriter(Lists.newArrayList());
    }
}