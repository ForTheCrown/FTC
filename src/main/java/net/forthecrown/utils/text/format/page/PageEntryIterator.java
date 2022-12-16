package net.forthecrown.utils.text.format.page;

import lombok.Getter;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.Validate;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator which iterates through a subsection of
 * entries in a list, array or other collection type object.
 * @param <T> The type of entry this iterator returns
 */
@Getter
public abstract class PageEntryIterator<T> implements Iterator<T> {
    /**
     * The current page being viewed
     * <p>
     * Keep in mind this behaves the same way indexes do.
     * Meaning the page number in functionally starts at 0.
     * And the page number displayed to the viewer is simply
     * {@link #getPage()} + 1
     */
    protected final int page;

    /**
     * The total size of the entries this iterator is
     * allowed to iterate through, essentially the size
     * of the {@link java.util.Collection} this iterator
     * goes through
     */
    protected final int size;

    /**
     * The amount of entries on a single page
     */
    protected final int pageSize;

    /**
     * The max page given the current {@link #getSize()} and {@link #getPageSize()}.
     * <p>
     * This value is always calculated by {@link #getMaxPage(int, int)}
     */
    protected final int maxPage;

    /**
     * The inclusive starting index of the current page's entries.
     * <p>
     * If this value is not supplied in the constructor along
     * with the {@link #end} field, then it'll be calculated
     * using the given {@link #page} and {@link #pageSize}
     */
    protected final int start;

    /**
     * The exclusive ending index of the current' page's entries.
     * <p>
     * If this value is not supplied in the constructor along
     * with the {@link #start} field, then it'll be calculated
     * with the given {@link #page} and {@link #pageSize}.
     * <p>
     * This value will always be clamped to [0..{@link #size}]
     */
    protected final int end;

    /**
     * The current index the iterator is at.
     * <p>
     * This is an absolute index, not relative to the
     * list, if you want a user-friendly index to display
     * to users, use {@link #getViewerIndex()}
     */
    protected int index;

    /**
     * Creates a page entry iterator.
     * <p>
     * This constructor calls the {@link #PageEntryIterator(int, int, int, int, int)}
     * with {@link #end} and {@link #start} calculated based on the given {@link #page}
     * and {@link #pageSize} values.
     *
     * @param page The current page we're on
     * @param size The total size of the backing list/array
     * @param pageSize The size of one page
     */
    public PageEntryIterator(int page, int size, int pageSize) {
        this(page, size, pageSize, page * pageSize, (page + 1) * pageSize);
    }

    /**
     * Creates a page entry iterator.
     * <p>
     * {@link #end} will be clamped to [0..{@link #getSize()}] and
     * this method will also validate that <code>page <= maxPage</code>,
     * if that condition fails it'll throw an exception
     *
     * @param page The page being viewed
     * @param size The total size of the backing list/array
     * @param pageSize The size of 1 page
     * @param start The inclusive starting index of the current page
     * @param end The exclusive ending index of the current page
     */
    public PageEntryIterator(int page, int size, int pageSize, int start, int end) {
        this.page = page;
        this.size = size;
        this.pageSize = pageSize;

        this.start = start;
        this.end = Mth.clamp(end, 0, size);

        this.maxPage = getMaxPage(pageSize, size);
        this.index = start;

        Validate.isTrue(page >= 0 && page <= maxPage, "Invalid page: %s, out of bounds [0..%s]", page, maxPage);
    }

    /**
     * Creates a page entry iterator with the givne list
     * as it's backing list.
     * @param list The list to use for entry accessing
     * @param page The current page being viewed
     * @param pageSize The size of 1 page
     * @param <T> The list's type
     * @return The created iterator.
     */
    public static <T> PageEntryIterator<T> of(List<T> list, int page, int pageSize) {
        return new PageEntryIterator<>(page, list.size(), pageSize) {
            @Override
            protected T getEntry(int index) {
                return list.get(index);
            }
        };
    }

    /**
     * Creates a page iterator that's reversed. This means that
     * entry 0 is at the end of the list while the last entry is
     * at the beginning of the list
     *
     * @param list The backing list to use
     * @param page The page being viewed
     * @param pageSize The size of 1 page
     * @param <T> The list's type
     * @return The created iterator
     */
    public static <T> PageEntryIterator<T> reversed(List<T> list, int page, int pageSize) {
        return new Reversed<>(page, list.size(), pageSize) {
            @Override
            protected T getEntry(int index) {
                return list.get(index);
            }
        };
    }

    /**
     * Gets the final page that can be displayed with the
     * given page size and list size.
     *
     * @param pageSize The size of 1 page
     * @param size The total size of entries
     * @return The final page that can be viewed
     */
    public static int getMaxPage(int pageSize, int size) {
        // Use this awful int -> double -> int conversion
        // to deal with list sizes that are and aren't exactly
        // divisible by the given page size parameter.
        // if divisible -> then int division would work exactly
        // if not divisible -> the divided value has to be rounded
        // up to accurately display the amount of viewable pages
        return (int) (Math.ceil(((double) size) / pageSize));
    }

    @Override
    public boolean hasNext() {
        return index >= start && index < end;
    }

    /**
     * Gets the viewer-friendly index of the
     * last returned {@link #next()} value.
     *
     * @return The viewer-friendly index of the last {@link #next()}
     */
    public int getViewerIndex() {
        // This method is here, so it can be
        // overriden by the Reverse subclass
        // Otherwise it just returns this index
        return getIndex();
    }

    /**
     * Tests if the current page is the last page
     * @return True, if the current page is the last page, false otherwise
     */
    public boolean isLastPage() {
        return page == (maxPage - 1) || page >= maxPage;
    }

    /**
     * Tests if the current page is the first page
     * @return True, if {@link #page} <= 0
     */
    public boolean isFirstPage() {
        return page == 0;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        // Get next entry and increment index
        // Since we're incrementing here after getting
        // the viewerIndex is the same as index
        return getEntry(index++);
    }

    /**
     * Gets an entry by the given index
     * @param index The index to get the entry of
     * @return The entry
     */
    protected abstract T getEntry(int index);

    /**
     * Page entry iterator implementation that iterates through
     * entries in a reverse order, meaning entry 0 is at the end
     * of the list instead of at the beginning.
     * @param <T> The iterator's type
     */
    public abstract static class Reversed<T> extends PageEntryIterator<T> {
        public Reversed(int page, int size, int pageSize) {
            super(page, size, pageSize);

        }

        @Override
        public T next() {
            super.next();
            return getEntry(size - index);
        }
    }
}