package net.forthecrown.text;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * A utility object which joins a list of {@link Component}s
 * together to form a single component text.
 */
@Accessors(chain = true)
public class TextJoiner implements ComponentLike {
    /**
     * A default delimiter, just ", "
     */
    public static final Component COMMA = Component.text(", ");

    /**
     * All the entries this joiner holds
     */
    private final List<Component> texts = new ObjectArrayList<>();

    /**
     * The text that will be prepended onto the
     * joiner's result.
     */
    @Getter @Setter
    private Component prefix;

    /**
     * The text that will be appended to the
     * joiner's result
     */
    @Getter @Setter
    private Component suffix;

    /**
     * The text that separates values
     * in the joiner's entry list
     */
    @Getter @Setter
    private Component delimiter;

    /**
     * The style to use on the result.
     */
    @Getter @Setter
    private Style style;

    /**
     * Creates a new joiner with a <code>null</code>
     * delimiter, suffix and prefix
     * @return The created joiner
     */
    public static TextJoiner newJoiner() {
        return new TextJoiner();
    }

    /**
     * Creates a new joiner with the given delimiter
     * @param delimiter The delimiter to use
     * @return The created joiner
     */
    public static TextJoiner on(Component delimiter) {
        return newJoiner().setDelimiter(delimiter);
    }

    /**
     * Creates a new joiner with the given delimiter
     * @param delimiter The delimiter to use
     * @return The created joiner
     */
    public static TextJoiner on(char delimiter) {
        return on(Component.text(delimiter));
    }

    /**
     * Creates a new joiner with the given delimiter
     * @param delimiter The delimiter to use
     * @return The created joiner
     */
    public static TextJoiner on(String delimiter) {
        return on(Component.text(delimiter));
    }

    /**
     * Creates a new joiner with {@link #COMMA} as the delimiter
     * @return The created joiner
     */
    public static TextJoiner onComma() {
        return on(COMMA);
    }

    /**
     * Creates a new joiner with {@link Component#space()}
     * as the delimiter.
     * @return The created joiner
     */
    public static TextJoiner onSpace() {
        return on(Component.space());
    }

    /**
     * Creates a new joiner with {@link Component#newline()}
     * as the delimiter.
     * @return The created joiner
     */
    public static TextJoiner onNewLine() {
        return on(Component.newline());
    }

    /**
     * Sets the color to use for the resulting text
     * @param color The color to use
     * @return This
     */
    public TextJoiner setColor(TextColor color) {
        if (style != null) {
            style = style.color(color);
        } else {
            style = Style.style(color);
        }

        return this;
    }

    /**
     * Adds the given text to this joiner
     * @param text The text to add
     * @return this
     */
    public TextJoiner add(ComponentLike text) {
        texts.add(text.asComponent());
        return this;
    }

    /**
     * Adds the given texts to this joiner
     * @param texts The texts to add
     * @return this
     */
    public TextJoiner add(ComponentLike... texts) {
        for (var t : Validate.noNullElements(texts)) {
            add(t);
        }

        return this;
    }

    /**
     * Adds all the stream's elements to this joiner
     * @param stream The stream to add to this joiner
     * @return This
     */
    public TextJoiner add(Stream<? extends ComponentLike> stream) {
        stream.forEach(this::add);
        return this;
    }

    /**
     * Adds all the elements of the given iterable to this joiner
     * @param components The components to add
     * @return This
     */
    public TextJoiner add(Iterable<? extends ComponentLike> components) {
        for (var t : components) {
            add(t);
        }

        return this;
    }

    /**
     * Builds the current entries into a single
     * joined component.
     *
     * @return The joined component
     */
    @Override
    public @NotNull Component asComponent() {
        TextComponent.Builder result = Component.text();

        if (style != null && !style.isEmpty()) {
            result.style(style);
        }

        // If we have a prefix, add it
        if (prefix != null) {
            result.append(prefix);
        }

        // Loop through elements, iterator itself
        // needed here for hasNext() check in loop
        var i = this.texts.iterator();
        while (i.hasNext()) {
            var c = i.next();

            result.append(c);

            // If there's a next element, and we have
            // a delimiter, append it onto the result
            if (i.hasNext() && delimiter != null) {
                result.append(delimiter);
            }
        }

        // If we have a suffix, append it
        // onto the text
        if (suffix != null) {
            result.append(suffix);
        }

        return result.build();
    }
}