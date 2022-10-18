package net.forthecrown.utils;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An abstract list iterator that skips over any and all
 * null items found
 * @param <T> The iterators type
 */
@RequiredArgsConstructor
public abstract class AbstractListIterator<T> implements ListIterator<T> {
    protected int pos = 0;
    protected int lastIndex = -1;

    // --- ABSTRACT METHODS ---

    protected abstract void add(int pos, T val);
    protected abstract @Nullable T get(int pos);
    protected abstract void set(int pos, @Nullable T val);
    protected abstract void remove(int pos);
    protected abstract int size();

    // --- METHODS ---

    protected boolean shouldSkip(@Nullable T value) {
        return value == null;
    }

    @Override
    public boolean hasNext() {
        // While in list limits and
        // while current entry should
        // be skipped: Skip!
        while (pos < size()
                && shouldSkip(get(pos))
        ) {
            pos++;
        }

        return pos < size();
    }

    @Override
    public boolean hasPrevious() {
        // Same thing as hasNext(), but in
        // reverse :D
        while (pos > -1
                && shouldSkip(get(pos))
        ) {
            pos--;
        }

        return pos > -1;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        return get(lastIndex = pos++);
    }

    @Override
    public T previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }

        return get(lastIndex = --pos);
    }

    @Override
    public int nextIndex() {
        return pos;
    }

    @Override
    public int previousIndex() {
        return lastIndex;
    }

    @Override
    public void remove() {
        if (lastIndex == -1) {
            throw new NoSuchElementException();
        }

        remove(lastIndex);
        lastIndex = -1;
    }

    @Override
    public void set(T t) {
        if (lastIndex == -1) {
            throw new NoSuchElementException();
        }

        set(lastIndex, t);
    }

    @Override
    public void add(T t) {
        if (lastIndex == -1) {
            throw new NoSuchElementException();
        }

        add(pos++, t);
        lastIndex = -1;
    }
}