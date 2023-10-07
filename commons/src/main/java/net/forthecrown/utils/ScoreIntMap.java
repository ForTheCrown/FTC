package net.forthecrown.utils;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.UUID;
import java.util.function.IntSupplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.Loggers;
import net.forthecrown.text.page.PagedIterator;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.Users;
import net.forthecrown.utils.ScoreIntMap.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreIntMap<K> implements Iterable<Entry<K>> {

  /**
   * The minimum value that can be stored in user maps, the value is 0
   */
  public static final int MINIMUM_VALUE = 0;

  /**
   * The comparator this map uses to sort entries
   */
  public static final Comparator<Entry> COMPARATOR = Comparator.naturalOrder();

  /**
   * The default return value supplier
   */
  @Getter
  private final IntSupplier defaultSupplier;

  /**
   * The UUID lookup backing map
   */
  private final Map<K, Entry<K>> entries = new HashMap<>();

  /**
   * The sorted entry map, used for displaying the top entries
   */
  private final ObjectList<Entry<K>> sortedList = new ObjectArrayList<>();

  /**
   * True, if the list has been saved and has not yet been saved. This value is also used to stop
   * unneeded save operations
   */
  @Getter
  private boolean unsaved = true;

  @Getter @Setter
  private boolean fatalErrors = true;

  @Setter
  @Getter
  private KeyValidator<K> validator;

  public ScoreIntMap() {
    this(() -> MINIMUM_VALUE);
  }

  public ScoreIntMap(IntSupplier defaultSupplier) {
    this.defaultSupplier = Objects.requireNonNull(defaultSupplier);
  }

  /**
   * Sets the uuid's value.
   * <p>
   * This method will also ensure that the given amount is equal to or greater
   * than 0.
   *
   * @param key    key to set the value of
   * @param amount amount to set the uuid's value to
   *
   * @throws IllegalArgumentException If a {@link KeyValidator} is present, and
   * the given UUID fails validation
   */
  public void set(K key, int amount) throws IllegalArgumentException {
    Objects.requireNonNull(key);

    if (validator != null) {
      String reason = validator.test(key);

      if (!Strings.isNullOrEmpty(reason)) {
        if (fatalErrors) {
          throw new IllegalArgumentException(reason);
        } else {
          Loggers.getLogger().error(reason);
          remove(key);
          return;
        }
      }
    }

    amount = Math.max(MINIMUM_VALUE, amount);

    var entry = entries.get(key);

    // UUID is not stored in the map
    if (entry == null) {
      entry = new Entry(key, amount);
      entries.put(key, entry);
      insert(entry);
    } else {
      // UUId stored in map, set entry's value
      // and resort map
      entry.value(amount);
      sortedList.sort(COMPARATOR);
    }

    unsaved = true;
  }

  /**
   * Gets a UUID's value in this map
   *
   * @param key The UUID to get the value of
   * @return The UUID's value, or {@link #getDefaultSupplier()} if the entry is not in this map
   */
  public int get(K key) {
    var entry = entries.get(key);

    if (entry == null) {
      return getDefaultSupplier().getAsInt();
    }

    return entry.value;
  }

  /**
   * Adds to the value of the given UUID
   *
   * @param key    key
   * @param amount amount to add
   */
  public void add(K key, int amount) {
    set(key, get(key) + amount);
  }

  /**
   * Removes from the value of the given UUID
   *
   * @param key    key to remove from
   * @param amount amount to remove
   */
  public void remove(K key, int amount) {
    set(key, Math.max(get(key) - amount, MINIMUM_VALUE));
  }

  /**
   * Removes the given UUID fom this map
   *
   * @param key key to remove
   */
  public void remove(K key) {
    var entry = entries.remove(key);

    if (entry != null) {
      sortedList.remove(entry);
    }
  }

  /**
   * Gets the amount of entries in this map
   *
   * @return The entry count
   */
  public int size() {
    return entries.size();
  }

  /**
   * Tests if the given UUID is contained within this map
   *
   * @param key Key to test
   * @return {@code true}, if the UUID is stored in this map
   */
  public boolean contains(K key) {
    return entries.containsKey(key);
  }

  /**
   * Tests if this map is empty
   *
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
  public ObjectListIterator<Entry<K>> iterator() {
    return ObjectIterators.unmodifiable(sortedList.iterator());
  }

  @Override
  public Spliterator<Entry<K>> spliterator() {
    return sortedList.spliterator();
  }

  /**
   * An iterator for displaying the entries from largest to smallest
   *
   * @param page     The page to view
   * @param pageSize The page's size
   * @return The page's iterator
   */
  public PagedIterator<Entry<K>> pageIterator(int page, int pageSize) {
    return PagedIterator.reversed(sortedList, page, pageSize);
  }

  /**
   * Sums up the total of this map's values
   *
   * @return The total value of this map
   */
  public long total() {
    return sortedList.stream().mapToLong(Entry::value).sum();
  }

  /**
   * Inserts the given entry into the sorted list
   *
   * @param entry The entry to insert
   */
  private void insert(Entry<K> entry) {
    int index = Collections.binarySearch(sortedList, entry);

    if (index < 0) {
      index = -index - 1;
    }

    sortedList.add(index, entry);
  }

  @Setter
  @Getter
  @Accessors(fluent = true)
  public static final class Entry<K> implements Comparable<Entry<K>> {

    private final K key;
    private int value;

    Entry(K key, int value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public int compareTo(@NotNull ScoreIntMap.Entry<K> o) {
      return Integer.compare(value, o.value);
    }
  }

  public interface KeyValidator<K> {

    KeyValidator<UUID> IS_PLAYER = key -> {
      UserLookup lookup = Users.getService().getLookup();
      var entry = lookup.getEntry(key);

      if (entry == null) {
        return "No played named '%s' exists".formatted(key);
      }

      return null;
    };

    /**
     * Tests the given UUID
     * @param key key to test
     *
     * @return The reason for the UUID being denied, or null/empty if
     *         the UUID is valid
     */
    @Nullable String test(K key);
  }
}