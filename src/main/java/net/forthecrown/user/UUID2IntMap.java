package net.forthecrown.user;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.SerializableObject;
import net.forthecrown.utils.text.format.page.PageEntryIterator;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.function.IntSupplier;

/**
 * A sorted UUID 2 int value map
 */
public class UUID2IntMap extends SerializableObject.Json
        implements Iterable<UUID2IntMap.Entry>
{
    /**
     * The minimum value that can be stored in user maps,
     * the value is 0
     */
    public static final int MINIMUM_VALUE = 0;

    /**
     * The comparator this map uses to sort entries
     */
    public static final Comparator<Entry> COMPARATOR = Comparator.naturalOrder();

    /** The default return value supplier */
    @Getter
    private final IntSupplier defaultSupplier;

    /** The UUID lookup backing map */
    private final Map<UUID, Entry> entries = new HashMap<>();

    /** The sorted entry map, used for displaying the top entries */
    private final ObjectList<Entry> sortedList = new ObjectArrayList<>();

    /**
     * True, if the list has been saved and has not yet been saved.
     * This value is also used to stop unneeded save operations
     */
    @Getter
    private boolean unsaved = true;

    public UUID2IntMap(Path path) {
        this(path, () -> MINIMUM_VALUE);
    }

    public UUID2IntMap(Path filePath, IntSupplier defaultSupplier) {
        super(filePath);
        this.defaultSupplier = defaultSupplier;
    }

    /**
     * Sets the uuid's value.
     * <p>
     * This method will also ensure that the given amount
     * is equal to or greater than 0.
     * @param uuid The UUID to set the value of
     * @param amount the amount to set the uuid's value to
     */
    public void set(UUID uuid, int amount) {
        amount = Math.max(MINIMUM_VALUE, amount);

        var entry = entries.get(uuid);

        // UUID is not stored in the map
        if (entry == null) {
            entry = new Entry(uuid);
            entry.setValue(amount);

            entries.put(uuid, entry);
            insert(entry);

            return;
        }

        // UUId stored in map, set entry's value
        // and resort map
        entry.setValue(amount);
        sortedList.sort(COMPARATOR);
        unsaved = true;
    }

    /**
     * Gets a UUID's value in this map
     * @param uuid The UUID to get the value of
     * @return The UUID's value, or {@link #getDefaultSupplier()}
     *         if the entry is not in this map
     */
    public int get(UUID uuid) {
        var entry = entries.get(uuid);

        if (entry == null) {
            return getDefaultSupplier().getAsInt();
        }

        return entry.getValue();
    }

    /**
     * Adds to the value of the given UUID
     * @param uuid The UUID
     * @param amount the amount to add
     */
    public void add(UUID uuid, int amount) {
        set(uuid, get(uuid) + amount);
    }

    /**
     * Removes from the value of the
     * given UUID
     * @param uuid The UUID to remove from
     * @param amount The amount to remove
     */
    public void remove(UUID uuid, int amount) {
        set(uuid, Math.max(get(uuid) - amount, MINIMUM_VALUE));
    }

    /**
     * Removes the given UUID fom this map
     * @param uuid The UUID to remove
     */
    public void remove(UUID uuid) {
        var entry = entries.get(uuid);

        if (entry != null) {
            sortedList.remove(entry);
        }
    }

    /**
     * Gets the amount of entries in this map
     * @return The entry count
     */
    public int size() {
        return entries.size();
    }

    /**
     * Tests if the given UUID is contained within this map
     * @param uuid The UUID to test
     * @return True, if the UUID is stored in this map
     */
    public boolean contains(UUID uuid) {
        return entries.containsKey(uuid);
    }

    /**
     * Tests if this map is empty
     * @return True, if {@link #size()} <= 0
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Clears the map
     */
    public void clear() {
        entries.clear();
        sortedList.clear();
        unsaved = true;
    }

    @NotNull
    @Override
    public ObjectListIterator<Entry> iterator() {
        return ObjectIterators.unmodifiable(sortedList.iterator());
    }

    @Override
    public Spliterator<Entry> spliterator() {
        return sortedList.spliterator();
    }

    /**
     * An iterator for displaying the entries from
     * largest to smallest
     * @param page The page to view
     * @param pageSize The page's size
     * @return The page's iterator
     */
    public PageEntryIterator<Entry> pageIterator(int page, int pageSize) {
        return PageEntryIterator.reversed(sortedList, page, pageSize);
    }

    /**
     * Sums up the total of this map's values
     * @return The total value of this map
     */
    public long total() {
        return sortedList.stream()
                .mapToLong(Entry::getValue)
                .sum();
    }

    /**
     * Inserts the given entry into the sorted list
     * @param entry The entry to insert
     */
    private void insert(Entry entry) {
        int index = Collections.binarySearch(sortedList, entry);

        if (index < 0) {
            index = -index - 1;
        }

        sortedList.add(index, entry);
    }

    @Override
    public void save() {
        if (!isUnsaved()) {
            return;
        }

        super.save();
    }

    public void save(JsonWrapper json) {
        for (var e: this) {
            json.add(e.getUniqueId().toString(), e.getValue());
        }

        unsaved = false;
    }

    public void load(JsonWrapper json) {
        clear();

        for (var e: json.entrySet()) {
            var id = UUID.fromString(e.getKey());
            set(id, e.getValue().getAsInt());
        }

        unsaved = false;
    }

    /**
     * A single entry within the user score map
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    @RequiredArgsConstructor
    public static class Entry implements Comparable<Entry> {
        private final UUID uniqueId;
        private int value;

        @Override
        public int compareTo(@NotNull UUID2IntMap.Entry o) {
            return Integer.compare(value, o.value);
        }
    }
}