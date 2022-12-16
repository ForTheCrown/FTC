package net.forthecrown.utils.text.format.page;

import net.forthecrown.core.Messages;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.ArrayIterator;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.ArrayUtils;

import static net.kyori.adventure.text.Component.space;

/**
 * A page header. These headers are made up of several {@link PageElement}s.
 * These elements are stored in arrays based on their type, the types are
 * as follows: <pre>
 *
 * 1. Prepend: These types of elements are prepended onto the
 *    header before any other elements
 *
 * 2. Repeating: These types are placed before and after the
 *    Title section
 *
 *    Note: This section's elements will be written in
 *          reverse order if being written after the
 *          title section
 *
 * 3. Title: This is placed inbetween the 2 repeat types
 *    and after the prepend and before the append types
 *
 * 4. Append: This is placed after the Title and the repeat
 *    types
 *
 * </pre>
 * With those types in place, the header can be customized to whatever
 * extend you might want.
 * <p>
 * When a header is created, {@link Messages#PAGE_BORDER} will be added
 * to the repeating element types automatically
 *
 * @see PageFormat
 * @see PageEntryIterator
 * @param <T> The type to format
 */
public class Header<T> implements PageElement<T> {

    // --- CONSTANTS ---

    static final int
            TYPE_PREPEND = 0,
            TYPE_REPEAT  = 1,
            TYPE_MIDDLE  = 2,
            TYPE_APPEND  = 3,
            TYPES_SIZE   = TYPE_APPEND + 1;

    // --- INSTANCE FIELDS ---

    /**
     * The element array, the outer array's index corresponds
     * to the type ID, and the inner array is just an array
     */
    private PageElement<T>[][] elements = new PageElement[TYPES_SIZE][0];

    // --- CONSTRUCTOR ---

    public Header() {
        // Add page border automatically
        repeating(Messages.PAGE_BORDER);
        repeating(space());
    }

    // --- STATIC CONSTRUCTORS ---

    /**
     * Creates an empty header
     * @param <T> The header's type
     * @return The created header
     */
    public static <T> Header<T> create() {
        return new Header<>();
    }

    /**
     * Creates a header with the given title
     * @param title The title to use
     * @param <T> The header's type
     * @return The created header
     */
    public static <T> Header<T> of(Component title) {
        return Header.<T>create()
                .repeating(space())
                .title(title);
    }

    // --- METHODS ---

    @Override
    public void write(PageEntryIterator<T> it, TextWriter writer) {
        write(elements[TYPE_PREPEND], writer, it, false);
        write(elements[TYPE_REPEAT],  writer, it, false);

        write(elements[TYPE_MIDDLE],  writer, it, false);

        write(elements[TYPE_REPEAT],  writer, it, true);
        write(elements[TYPE_APPEND],  writer, it, false);
    }

    /**
     * Writes the elements in the given array
     * @param elements The elements to write
     * @param writer The writer to write to
     * @param it The page being iterated through
     * @param reverse Whether to reverse the given elements array
     */
    private static <T> void write(PageElement<T>[] elements, TextWriter writer, PageEntryIterator<T> it, boolean reverse) {
        if (elements == null || elements.length <= 0) {
            return;
        }

        if (reverse) {
            elements = elements.clone();
            ArrayUtils.reverse(elements);
        }

        var iter = ArrayIterator.unmodifiable(elements);
        while (iter.hasNext()) {
            iter.next().write(it, writer);
        }
    }

    /**
     * Adds an element with the given type
     * @param type The type to add
     * @param component The component to add
     * @return This
     */
    private Header<T> addElement(int type, Component component) {
        return addElement(type, (it, writer) -> writer.write(component));
    }

    /**
     * Adds an element with the given type
     * @param type The type to add
     * @param element The element to add
     * @return This
     */
    private Header<T> addElement(int type, PageElement<T> element) {
        var elements = this.elements[type];

        if (elements == null) {
            elements = new PageElement[1];
            elements[0] = element;
        } else {
            elements = ArrayUtils.add(elements, element);
        }

        this.elements[type] = elements;
        return this;
    }

    /**
     * Adds an element that will be written
     * before other elements
     * @param element The element to prepend to the header
     * @return This
     */
    public Header<T> prepend(PageElement<T> element) {
        return addElement(TYPE_PREPEND, element);
    }

    /**
     * Adds an element that will be written
     * before other elements
     * @param component The text to prepend
     * @return This
     */
    public Header<T> prepend(Component component) {
        return addElement(TYPE_PREPEND, component);
    }

    /**
     * Adds an element that will be written last
     * @param element The element to add
     * @return This
     */
    public Header<T> append(PageElement<T> element) {
        return addElement(TYPE_APPEND, element);
    }

    /**
     * Adds an element that will be written last
     * @param component The text to append
     * @return This
     */
    public Header<T> append(Component component) {
        return addElement(TYPE_APPEND, component);
    }

    /**
     * Adds an element that will be written before
     * and after the title
     * @param element The element to add
     * @return This
     */
    public Header<T> repeating(PageElement<T> element) {
        return addElement(TYPE_REPEAT, element);
    }

    /**
     * Adds an element that will be written before
     * and after the title
     * @param component The text to add
     * @return This
     */
    public Header<T> repeating(Component component) {
        return addElement(TYPE_REPEAT, component);
    }

    /**
     * Adds a title element that will be written
     * between the two repeating nodes and
     * between the prepend/append types.
     * @param element The element to add
     * @return This
     */
    public Header<T> title(PageElement<T> element) {
        return addElement(TYPE_MIDDLE, element);
    }

    /**
     * Adds a title element that will be written
     * between the two repeating nodes and
     * between the prepend/append types.
     * @param component The text to add
     * @return This
     */
    public Header<T> title(Component component) {
        return addElement(TYPE_MIDDLE, component);
    }
}