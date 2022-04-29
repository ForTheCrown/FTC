package net.forthecrown.economy;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

/**
 * A constantly sorted map of balances.
 * <p></p>
 * This implementation isn't actually a map, rather it is a
 * list of entries that is constantly reorganized and sorted
 * whenever a balance is modified, added or removed. This
 * keeps the map sorted at all times.
 */
public class SortedBalanceMap implements BalanceMap {
    private final IntSupplier defaultAmount;
    private final int expectedSize;

    private Balance[] entries;
    private int size;

    public SortedBalanceMap(int expectedSize, IntSupplier defaultAmount) {
        this.defaultAmount = defaultAmount;
        this.expectedSize = expectedSize;
        entries = new Balance[expectedSize];
    }

    @Override
    public IntSupplier getDefaultSupplier() {
        return defaultAmount;
    }

    @Override
    public boolean contains(UUID id) {
        return getIndex(id) != -1;
    }

    @Override
    public void remove(UUID id) {
        int index = getIndex(id);
        if (index == -1) return;

        //Remove entry
        entries[index] = null;
        size--;

        //If the index wasn't the last entry in the list,
        //collapse array at index
        if (size != index) {
            //Collapse array at index so there's no empty spaces
            System.arraycopy(entries, index + 1, entries, index, entries.length - index - 1);

            //Nullify last entry, as it's a copy of length - 2 now
            entries[entries.length - 1] = null;
        }
    }

    @Override
    public int get(UUID id) {
        int index = getIndex(id);
        if (index == -1) return getDefaultAmount();

        return entries[index].getValue();
    }

    @Override
    public Balance getEntry(int index) {
        validateIndex(index);
        return entries[index];
    }

    public int getIndex(UUID id) {
        // Slow, but IDK how better to do it.
        // Go through list, checking each end of the list
        // to find the entry

        int half = size >> 1;

        // Will be 1 if not divisible, 0 if divisible
        int divCorrection = size - (half + half);

        // Use divCorrection to account for odd sized lists
        for (int i = 0; i < half + divCorrection; i++) {
            //Check front half
            Balance entry = getEntry(i);
            if (entry != null && entry.getUniqueId().equals(id)) return i;

            //Check last half
            int oppositeEnd = size - 1 - i;
            entry = getEntry(oppositeEnd);
            if (entry != null && entry.getUniqueId().equals(id)) return oppositeEnd;
        }

        //Not found
        return -1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        entries = new Balance[0];
        size = 0;
    }

    @Override
    public void put(UUID id, int amount) {
        int index = getIndex(id);
        //Either get the entry, and update its value, or create it
        Balance entry = index == -1 ? new Balance(id, amount) : getEntry(index).setValue(amount);

        //If new entry
        if (index == -1) {
            index = size;

            //Increment size
            size++;

            //If array size has to be increased
            if (size >= entries.length) {
                Balance[] copy = entries;                           //Copy old entries
                entries = new Balance[newSize(copy.length + 1)];    //Make new array with bigger size
                System.arraycopy(copy, 0, entries, 0, copy.length); //Copy all entries from copy to new array
            }

            setEntry(index, entry);
        }

        checkSorted(index);
    }

    private int newSize(int length) {
        return (size / expectedSize + 1) * expectedSize;
    }

    private void checkSorted(int index) {
        int moveDir = moveDir(index);
        if (moveDir == 0) return;

        moveInDir(index, moveDir);
    }

    private void setEntry(int index, Balance bal) {
        entries[index] = bal;
    }

    private void moveInDir(int index, int dir) {
        int newIndex = index + dir;

        Balance entry = getEntry(newIndex);
        Balance newE = getEntry(index);

        setEntry(newIndex, newE);
        setEntry(index, entry);

        checkSorted(newIndex);
    }

    private int moveDir(int index) {
        Balance entry = getEntry(index);

        int towardsTop = index + 1;
        if (isInList(towardsTop)) {
            Balance top = getEntry(towardsTop);
            if (top != null && top.compareTo(entry) == 1) return 1;
        }

        int towardsBottom = index - 1;
        if (isInList(towardsBottom)) {
            Balance bottom = getEntry(towardsBottom);
            if (bottom != null && bottom.compareTo(entry) == -1) return -1;
        }

        return 0;
    }

    private boolean isInList(int index) {
        return index >= 0 && index < size;
    }

    @Override
    public long getTotalBalance() {
        int defAmount = getDefaultSupplier().getAsInt();

        return Arrays.stream(entries)
                .filter(b -> b != null && b.getValue() > defAmount)
                .mapToInt(Balance::getValue)
                .sum();
    }

    @Override
    public Stream<BalanceReader> readerStream() {
        return Arrays.stream(entries, 0, size);
    }

    private void validateIndex(int index) {
        if (!isInList(index)) throw new IndexOutOfBoundsException("Index " + index + " not in range [0 " + size + ")");
    }
}