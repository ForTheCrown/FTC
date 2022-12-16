package net.forthecrown.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;

import java.util.*;

public class WeightedList<T> {
    private final List<Entry<T>> values = new ObjectArrayList<>();

    @Getter
    private int totalWeight = 0;

    public void addAll(WeightedList<T> other) {
        for (var e: other.values) {
            add(e.weight, e.value);
        }
    }

    public void add(int weight, T value) {
        totalWeight += weight;
        values.add(new Entry<>(value, weight));
    }

    private void remove(Entry<T> entry) {
        int index = values.indexOf(entry);

        if (index == -1) {
            return;
        }

        remove(index);
    }

    private void remove(int index) {
        Objects.checkIndex(index, values.size());
        var val = values.remove(index);
        totalWeight -= val.weight;
    }

    public T get(Random random) {
        Entry<T> value = findRandom(random);

        if (value == null) {
            return null;
        }

        return value.value;
    }

    private Entry<T> findRandom(Random random) {
        if (isEmpty()) {
            return null;
        }

        int value = random.nextInt(0, totalWeight);

        for (Entry<T> p : values) {
            if ((value -= p.weight) <= 0) {
                return p;
            }
        }

        return null;
    }

    public void clear() {
        totalWeight = 0;
        values.clear();
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public int size() {
        return values.size();
    }

    public Iterator<T> iterator(Random random) {
        return new WeightedIterator(random);
    }

    /* ----------------------------- SUB CLASSES ------------------------------ */

    private record Entry<T>(T value, int weight) {}

    private class WeightedIterator implements Iterator<T> {
        private final Random random;
        private final WeightedList<T> remaining;

        private Entry<T> current;

        public WeightedIterator(Random random) {
            this.random = random;

            remaining = new WeightedList<>();
            remaining.addAll(WeightedList.this);
        }

        @Override
        public boolean hasNext() {
            return !remaining.isEmpty();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (remaining.values.size() == 1) {
                current = remaining.values.get(0);
                remaining.clear();

                return current.value;
            }

            current = remaining.findRandom(random);
            remaining.remove(current);

            return current.value;
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }

            WeightedList.this.remove(current);
            current = null;
        }
    }
}