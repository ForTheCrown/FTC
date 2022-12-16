package net.forthecrown.utils.text.writer;

import lombok.Getter;
import lombok.Setter;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;

import java.util.Iterator;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A Chat Writer is a common interface for writing multi-line
 * components, blocks of texts, formatted info lists or whatever
 * else to a common interface which could then be displayed onto
 * whatever the writer implementation was written for.
 * <p>
 * Currently, there are text writers like {@link LoreWriter} which
 * depend on {@link #onNewLine()} being called appropriately, the
 * issue this faces however, is that currently, if {@link #write(ComponentLike)}
 * is called with a text that is exactly equal to {@link Component#newline()}
 * then it detects the new line, else it will fail to detect any
 * newlines being written
 * <p>
 * {@link TextWriters} provides static methods for creating these
 * writers.
 * @see TextWriters
 */
public abstract class TextWriter implements ComponentLike {
    /** Default field separator: ': ' */
    private static final Component DEF_FIELD_SEPARATOR = Component.text(": ");

    /** New line pattern */
    public static final Pattern NEW_LINE_PATTERN = Pattern.compile("\n");

    /** True, if the current line is empty, false otherwise */
    @Getter
    protected boolean lineEmpty = true;

    /** The style a field has */
    @Getter @Setter
    protected Style fieldStyle = Style.empty();

    /** The style a field's value has */
    @Getter @Setter
    protected Style fieldValueStyle = Style.empty();

    /** The separator {@link #field(ComponentLike, ComponentLike)} uses to separate fields and values */
    @Getter @Setter
    protected Component fieldSeparator = DEF_FIELD_SEPARATOR;

    /** Empty constructor */
    public TextWriter() {}

    /** Copy constructor */
    public TextWriter(TextWriter other) {
        this.fieldSeparator = other.getFieldSeparator();
        this.fieldStyle = other.getFieldStyle();
        this.lineEmpty = other.isLineEmpty();
        this.fieldValueStyle = other.getFieldValueStyle();
    }

    protected abstract void onNewLine();
    protected abstract void onClear();
    protected abstract void onWrite(Component text);

    /**
     * Writes the given text into this writer.
     * <p>
     * If the given text is a new line text, then
     * it calls the {@link #onNewLine()} callback
     * and moves to the next line, else, it calls
     * the {@link #onWrite(Component)} callback.
     *
     * @param text The text to write
     */
    public void write(ComponentLike text) {
        Objects.requireNonNull(text, "Text was null");
        var component = text.asComponent();

        if (component.equals(Component.newline())) {
            newLine();
            return;
        }

        Iterator<Component> it = Text.split(NEW_LINE_PATTERN, component)
                .iterator();

        while (it.hasNext()) {
            var next = it.next();
            onWrite(next);
            lineEmpty = false;

            if (it.hasNext()) {
                newLine();
            }
        }
    }

    /**
     * Clears the writer's written components
     */
    public void clear() {
        lineEmpty = true;
        onClear();
    }

    /**
     * Writes a key value pair to this writer
     * @param field The key
     * @param value  The value of the key
     */
    public void field(ComponentLike field, ComponentLike value) {
        if (!isLineEmpty()) {
            newLine();
        }

        write(
                Component.text()
                        .style(fieldStyle)
                        .append(field, fieldSeparator)
        );

        // Don't write empty values
        if (!Objects.equals(Component.empty(), value)) {
            write(
                    Component.text()
                            .style(fieldValueStyle)
                            .append(value)
            );
        }
    }

    /**
     * Writes a key value par to this writer
     * @param field The key
     * @param value The value of the key
     */
    public void field(String field, ComponentLike value) {
        field(Text.renderString(field), value);
    }

    /**
     * Writes a key value par to this writer
     * @param field The key
     * @param value The value of the key
     */
    public void field(String field, Object value) {
        field(field, Text.valueOf(value));
    }

    /**
     * Writes the given string as a single line
     * with an empty style.
     * <p>
     * If {@link #isLineEmpty()} returns false, then
     * this will call {@link #newLine()} and then
     * write the given text. After the given text
     * is written, {@link #newLine()} is not called
     * again by this method.
     *
     * @param s The text to write
     * @see #line(ComponentLike)
     * @see #line(String, Style)
     */
    public void line(String s) {
        line(s, Style.empty());
    }

    /**
     * Writes the given string as a single line
     * with the given text
     * <p>
     * If {@link #isLineEmpty()} returns false, then
     * this will call {@link #newLine()} and then
     * write the given text. After the given text
     * is written, {@link #newLine()} is not called
     * again by this method.
     *
     * @param s The text to write
     * @param color The color to write
     * @see #line(String, Style)
     * @see #line(ComponentLike)
     */
    public void line(String s, TextColor color) {
        line(s, Style.style(color));
    }

    /**
     * Writes the given string with the given style
     * as a single line.
     * <p>
     * If {@link #isLineEmpty()} returns false, then
     * this will call {@link #newLine()} and then
     * write the given text. After the given text
     * is written, {@link #newLine()} is not called
     * again by this method.
     *
     * @param s The string to write
     * @param style The style to apply to the text
     */
    public void line(String s, Style style) {
        line(Text.renderString(s).style(style));
    }

    /**
     * Writes the given text as a single line
     * <p>
     * If {@link #isLineEmpty()} returns false, then
     * this will call {@link #newLine()} and then
     * write the given text. After the given text
     * is written, {@link #newLine()} is not called
     * again by this method.
     *
     * @param text The text to write
     */
    public void line(ComponentLike text) {
        if (!isLineEmpty()) {
            newLine();
        }

        write(text);
    }

    /**
     * Moves the writer to the next line.
     * <p>
     * Implementations of the writer interface may rely
     * on this method being called to properly
     * format the writer's results. One example of this
     * is {@link LoreWriter}
     */
    public void newLine() {
        onNewLine();
        lineEmpty = true;
    }

    /**
     * Write an empty whitespace character
     */
    public void space() {
        write(Component.space());
    }

    /**
     * Writes a string to this writer
     * @param s The string to write
     * @param style The style to use for the text
     *              being written, this will be
     *              applied after the string is
     *              converted to a Component
     */
    public void write(String s, Style style) {
        write(
                Text.renderString(s)
                        .style(style)
        );
    }

    /**
     * Writes the given string with the given
     * color as the base color
     * @param s The string to write
     * @param color The color to use as a base
     */
    public void write(String s, TextColor color) {
        write(s, Style.style(color));
    }

    /**
     * Writes the given string to this writer
     * with an empty style
     * @param s The string to write
     */
    public void write(String s) {
        write(s, Style.empty());
    }

    public void formatted(ComponentLike format, Object... args) {
        write(Text.format(format.asComponent(), args));
    }

    /**
     * Delegate method for {@link Text#format(String, Object...)}.
     * Writes a formatted message to this writer
     * @param format The format to use
     * @param args The arguments to use
     */
    public void formatted(String format, Object... args) {
        write(Text.format(format, args));
    }

    /**
     * Delegate method for {@link Text#format(String, TextColor, Object...)}.
     * Writes a formatted message to this writer
     * @param format The format to use
     * @param color The color to use
     * @param args The arguments to use
     */
    public void formatted(String format, TextColor color, Object... args) {
        write(Text.format(format, color, args));
    }

    public void formattedLine(String format, Object... args) {
        formattedLine(format, Style.empty(), args);
    }

    public void formattedLine(String format, TextColor color, Object... args) {
        formattedLine(format, Style.style(color), args);
    }

    public void formattedLine(String format, Style style, Object... args) {
        formattedLine(Text.renderString(format).style(style), args);
    }

    public void formattedLine(ComponentLike like, Object... args) {
        line(Text.format(like.asComponent(), args));
    }

    public String getString() {
        return Text.toString(asComponent());
    }

    /**
     * Gets a plain text representation of what
     * this writer has written so far
     * @return Plain text result of this component
     */
    public String getPlain() {
        return Text.plain(asComponent());
    }

    /**
     * Creates a writer which writes to the same
     * source as this writer with a prefix after
     * every new line write.
     *
     * @param prefix The prefix
     * @return The prefixed writer
     */
    public PrefixedWriter withPrefix(ComponentLike prefix) {
        return new PrefixedWriter(
                this,
                Objects.requireNonNull(prefix)
                        .asComponent()
        );
    }

    public PrefixedWriter withIndent(int indent) {
        return withPrefix(Component.text(" ".repeat(indent)));
    }

    public PrefixedWriter withIndent() {
        return withIndent(2);
    }
}