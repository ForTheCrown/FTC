package net.forthecrown.utils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArrayIterator<T> extends AbstractListIterator<T> {
    final T[] values;
    final boolean modifiable;

    public static <T> ArrayIterator<T> modifiable(T[] values) {
        return new ArrayIterator<>(values, true);
    }

    public static <T> ArrayIterator<T> unmodifiable(T[] values) {
        return new ArrayIterator<>(values, false);
    }

    @Override
    protected void add(int location, T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void set(int location, T t) {
        ensureMutable();
        values[location] = t;
    }

    @Override
    protected T get(int location) {
        return values[location];
    }

    @Override
    protected void remove(int location) {
        set(location, null);
    }

    private void ensureMutable() {
        if (!modifiable) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    protected int size() {
        return values.length;
    }
}