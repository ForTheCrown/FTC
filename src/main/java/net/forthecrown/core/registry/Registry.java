package net.forthecrown.core.registry;

import static net.forthecrown.core.registry.Keys.VALID_KEY_REGEX;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectCollections;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.user.property.PropertyMap;
import net.forthecrown.user.property.UserProperty;
import net.forthecrown.utils.AbstractListIterator;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.Util;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.index.qual.NonNegative;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A registry is a map of string keys to entries and an array of entry id 2 entries at the same
 * time.
 * <p>
 * Registry's provide a way of mapping a string key with a certain format, (See
 * {@link Keys#VALID_KEY_REGEX}), to a value and then assigning that value an integer id. That
 * integer ID can be used then to index the value outside this registry, like in the case of
 * {@link UserProperty}, where the id given by the registry the property is registered into, is used
 * as the index of that property in the property value array in {@link PropertyMap}.
 * <p>
 * All values are also mapped to their entry, and as such, having objects which do not know their
 * own key is viable, as any key lookups won't require the registry to loop through all entries to
 * find the key.
 * <p>
 * Registries use a specific type of entry, that being {@link Holder}, which is made up of 3
 * immutable values, the key, the ID and the value of the entry itself
 * <p>
 * Registry's can also be created in a way that allows them to be 'frozen' aka, made unmodifiable,
 * this is done with {@link #freeze()}. Creating a frozen registry is done with
 * {@link Registries#newFreezable()}. However, if a registry is created with
 * {@link Registries#newRegistry()}, then calling {@link #freeze()} will throw an exception, as the
 * registry is not allowed to be closed down.
 * <p>
 * ID-mapping in registries is done with a single {@link Holder} array, where the index of said
 * array is the id of an entry, entries are automatically generated with IDs using
 * {@link #findNextId()}, however if required, a custom ID can be specified. This implementation
 * means that no extra List objects are created and the ID array only contains as much data as it
 * has to.
 * <p>
 * Registries also support some bare-bones serialization functions for saving an entry of a registry
 * by its associated key. That being said, if you require something which serializes the values of
 * the registry itself, then that serializer must be custom written
 *
 * @param <V> The registry's type
 * @see #register(Holder)
 * @see #register(String, Object, int)
 * @see #register(String, Object)
 * @see Keys#VALID_KEY_REGEX
 * @see Registries
 * @see Holder
 */
public class Registry<V> implements Iterable<V> {
  /* ----------------------------- CONSTANTS ------------------------------ */

  private static final Logger LOGGER = Loggers.getLogger();

  /**
   * An empty and immutable holder array
   */
  @SuppressWarnings("rawtypes")
  private static final Holder[] EMPTY_ARRAY = new Holder[0];

  /**
   * The maximum amount of attempts {@link #getRandom(Random, Predicate)} makes to find a valid
   * entry
   */
  private static final int MAX_RANDOM_ATTEMPTS = 512;

  /**
   * The ID generator used to generate ID numbers for registries
   *
   * @see #registryId
   */
  private static final AtomicInteger nextRegistryId = new AtomicInteger(0);

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  /**
   * Determines whether this registry may be frozen
   */
  @Getter
  private final boolean freezingAllowed;

  /**
   * Key to entry lookup map
   */
  private final Object2ObjectMap<String, Holder<V>> byKey
      = new Object2ObjectOpenHashMap<>();

  /**
   * Value to entry lookup map
   */
  private final Object2ObjectMap<V, Holder<V>> byValue
      = new Object2ObjectOpenHashMap<>();

  /**
   * ID to entry lookup array
   */
  @SuppressWarnings("unchecked")
  private Holder<V>[] byId = EMPTY_ARRAY;

  @Getter
  private RegistryListener<V> listener;

  /**
   * True, if the registry has been frozen and was made immutable, false otherwise
   */
  @Getter
  private boolean frozen;

  /**
   * This registry's ID
   * <p>
   * Generated by incrementing the value of the static {@link #nextRegistryId}
   * <p>
   * This ID is used in the {@link #equals(Object)} method to test if a registry is equal to this
   * one. Also used in the {@link #hashCode()} method as the return value.
   */
  @Getter
  private final int registryId;

  /* --------------------------- CONSTRUCTORS ---------------------------- */

  // Only ever constructed in Registries with
  // factory methods, so this is package-private
  Registry(boolean freezingAllowed) {
    this.registryId = nextRegistryId.getAndIncrement();
    this.freezingAllowed = freezingAllowed;
  }

  /* --------------------------- REGISTRATION ---------------------------- */

  /**
   * Registers the given key and value into this registry, this uses {@link #findNextId()} to
   * generate an ID for the given value and key as well.
   * <p>
   * This method will first ensure that the registry is not frozen, then will test for naming
   * conflicts and only after all those tests have passed, does it register the given holder into
   * the 3 lookup objects.
   * <p>
   * This function will also throw an {@link IllegalArgumentException} in one of
   * the following scenarios: <pre>
   * 1. The registry is frozen
   * 2. The registry already contains an entry with the given key
   * 3. The given key does not match the key Regex
   * </pre>
   * It is worth nothing that if the given key is a namespaced key string, then the namespace of the
   * key will be removed before the key validation test is ran. That being said, if the result of
   * the namespace is null, this will again throw a {@link IllegalArgumentException}
   *
   * @param key   The key of the value
   * @param value The value itself
   * @return The registered entry
   * @throws IllegalArgumentException Can be thrown in one of the above-mentioned scenarios
   * @throws NullPointerException     If the given value or key were null
   * @see #register(String, Object, int)
   */
  public @NotNull Holder<V> register(@Pattern(VALID_KEY_REGEX) String key,
                                     @NotNull V value
  ) throws IllegalArgumentException,
      NullPointerException {
    return register(key, value, findNextId());
  }

  /**
   * Registers the given key, value and ID into this registry
   * <p>
   * This method will first ensure that the registry is not frozen, then will test for naming
   * conflicts and only after all those tests have passed, does it register the given holder into
   * the 3 lookup objects.
   * <p>
   * This function will also throw an {@link IllegalArgumentException} in one of
   * the following scenarios: <pre>
   *  1. The registry is frozen
   *  2. The registry already contains an entry with the given key
   *  3. The registry already contains an entry with the given ID
   *  4. The given ID is invalid, more specifically, less than 0
   *  5. The given key does not match the key Regex
   * </pre>
   * It is worth nothing that if the given key is a namespaced key string, then the namespace of the
   * key will be removed before the key validation test is ran. That being said, if the result of
   * the namespace is null, this will again throw a {@link IllegalArgumentException}
   *
   * @param key   The key of the value to register
   * @param value The value itself
   * @param id    The ID of the value
   * @return The registered entry
   * @throws IllegalArgumentException Can be thrown in one of the above-mentioned scenarios
   * @throws NullPointerException     If the given value or key were null
   * @see #register(Holder)
   */
  public @NotNull Holder<V> register(@Pattern(VALID_KEY_REGEX) String key,
                                     @NotNull V value,
                                     @NonNegative int id
  ) throws IllegalArgumentException,
      NullPointerException {
    return register(new Holder<>(removeNamespace(key).intern(), id, value));
  }

  /**
   * Registers the given holder into this map
   * <p>
   * This method will first ensure that the registry is not frozen, then will test for naming
   * conflicts and only after all those tests have passed, does it register the given holder into
   * the 3 lookup objects.
   * <p>
   * This function will also throw an {@link IllegalArgumentException} in one of
   * the following scenarios: <pre>
   * 1. The registry is frozen
   * 2. The registry already contains an entry with the given key
   * 3. The registry already contains an entry with the given ID
   * 4. The given ID is invalid, more specifically, less than 0
   * 5. The given key does not match the key Regex
   * </pre>
   *
   * @param holder The holder to register
   * @return The registered holder, aka, the input
   * @throws IllegalArgumentException Can be thrown in one of the above-mentioned scenarios
   * @throws NullPointerException     If either the holder, holder's value or holder's key were
   *                                  null
   */
  private @NotNull Holder<V> register(@NotNull Holder<V> holder)
      throws IllegalArgumentException, NullPointerException {
    testFrozen();

    // Ensure no nulls
    Objects.requireNonNull(holder.getValue(), "Value was null");
    Objects.requireNonNull(holder.getKey(), "Key was null");

    // Ensure there's a valid key being used
    Keys.ensureValid(holder.getKey());

    // Test for naming/ID conflicts
    if (contains(holder.getKey())) {
      throw Util.newException("Registry already contains mapping for '%s'",
          holder.getKey()
      );
    }

    if (contains(holder.getId())) {
      throw Util.newException(
          "Registry already contains mapping for id %s, key: '%s'",
          holder.getId(), holder.getKey()
      );
    }

    // Ensure we don't have an invalid ID
    if (holder.getId() < 0) {
      throw Util.newException("Invalid index for entry '%s': %s",
          holder.getKey(), holder.getId()
      );
    }

    // Insert holder into registry lookup objects
    byId = ObjectArrays.ensureCapacity(byId, holder.getId() + 1);

    byId[holder.getId()] = holder;
    byKey.put(holder.getKey(), holder);
    holder.setRegistry(this);

    var existing = byValue.put(holder.getValue(), holder);

    if (listener != null) {
      listener.onRegister(holder);
    }

    if (existing != null) {
      LOGGER.warn(
          "Registry value hash collision! Entry '{}' replaced " +
              "'{}' in the value-lookup map",

          holder.getKey(), existing.getKey()
      );
    }

    return holder;
  }

  /**
   * Finds the next valid ID to register an entry at in this registry
   *
   * @return The next valid ID
   */
  public int findNextId() {
    // Empty ID array, next ID is 0
    if (byId.length == 0) {
      return 0;
    }

    // Find first non-null entry
    for (int i = 0; i < byId.length; i++) {
      if (byId[i] == null) {
        return i;
      }
    }

    // No non-null entries -> ID = array size
    return byId.length;
  }

  /* ------------------------ VALUE MODIFICATION ------------------------- */

  /**
   * Removes an entry from this registry with the given key. If the given key is not contained in
   * this registry, this will simply return false
   * <p>
   * If the remove operation is successfully, then the ID lookup array is not changed, as doing so
   * would invalidate every ID that came after the removed entry's
   *
   * @param key The key of the element to remove
   * @return True, if this registry was changed as a result of this method call, false otherwise
   * @throws IllegalArgumentException If the registry is frozen
   * @see #removeHolder(Optional)
   */
  public boolean remove(@Pattern(VALID_KEY_REGEX) String key)
      throws IllegalArgumentException {
    return removeHolder(getHolder(key));
  }

  /**
   * Removes an entry from this registry with the given ID. If the given ID is not contained in this
   * registry, this will simply return false
   * <p>
   * If the remove operation is successfully, then the ID lookup array is not changed, as doing so
   * would invalidate every ID that came after the removed entry's
   *
   * @param id The ID of the element to remove
   * @return True, if this registry was changed as a result of this method call, false otherwise
   * @throws IllegalArgumentException If the registry is frozen
   * @see #removeHolder(Optional)
   */
  public boolean remove(int id) throws IllegalArgumentException {
    return removeHolder(getHolder(id));
  }

  /**
   * Removes an entry from this registry with the given value. If the given value is not contained
   * in this registry, this will simply return false
   * <p>
   * If the remove operation is successfully, then the ID lookup array is not changed, as doing so
   * would invalidate every ID that came after the removed entry's
   *
   * @param value The value to remove
   * @return True, if this registry was changed as a result of this method call, false otherwise
   * @throws IllegalArgumentException If the registry is frozen
   * @see #removeHolder(Optional)
   */
  public boolean removeValue(V value) throws IllegalArgumentException {
    return removeHolder(getHolderByValue(value));
  }

  /**
   * Removes the given entry from this registry
   * <p>
   * If the remove operation is successfully, then the ID lookup array is not changed, as doing so
   * would invalidate every ID that came after the removed entry's
   *
   * @param optional The entry to remove.
   * @return True, if this registry was changed as a result of this method call, false otherwise
   * @throws IllegalArgumentException If the registry is frozen
   */
  private boolean removeHolder(Optional<Holder<V>> optional)
      throws IllegalArgumentException {
    testFrozen();

    if (optional.isEmpty()) {
      return false;
    }

    var holder = optional.get();
    return _remove(holder);
  }

  private boolean _remove(Holder<V> holder) {
    // Set entry to null and don't collapse array
    // if we collapsed it, every entry's ID after
    // this one would be invalid
    byId[holder.getId()] = null;

    // Remove from lookup maps
    byKey.remove(holder.getKey());
    byValue.remove(holder.getValue());

    if (listener != null) {
      listener.onUnregister(holder);
    }

    holder.setRegistry(null);
    return true;
  }

  /**
   * Removes all entries that match the given predicate
   *
   * @param predicate The predicate that determines which entries to remove
   * @return True, if this registry changed at all, false otherwise
   * @throws IllegalArgumentException If this registry is frozen
   * @throws NullPointerException     If the predicate was null
   */
  public boolean removeIf(@NotNull Predicate<Holder<V>> predicate)
      throws IllegalArgumentException, NullPointerException {
    Objects.requireNonNull(predicate, "Null predicate");
    testFrozen();

    var it = byKey.values().iterator();
    boolean changed = false;

    while (it.hasNext()) {
      var next = it.next();

      if (!predicate.test(next)) {
        continue;
      }

      changed = true;
      it.remove();
      _remove(next);
    }

    return changed;
  }

  /**
   * Clears all mappings within this registry
   *
   * @throws IllegalStateException If the registry is frozen
   */
  @SuppressWarnings("unchecked")
  public void clear() throws IllegalArgumentException {
    testFrozen();

    if (listener != null) {
      byKey.values().forEach(listener::onUnregister);
    }

    byValue.clear();
    byKey.clear();
    byId = EMPTY_ARRAY;
  }

  /* ----------------------------- FREEZING ------------------------------ */

  /**
   * Freezes this registry, making it immutable to further changes
   *
   * @throws IllegalArgumentException If {@link #isFreezingAllowed()} returns false.
   */
  public void freeze() throws IllegalArgumentException {
    if (!isFreezingAllowed()) {
      throw Util.newException("This registry does not allow freezing");
    }

    frozen = true;
  }

  /**
   * Tests if the registry is frozen or not.
   * <p>
   * For this method to actually throw the exception it requires that both {@link #isFrozen()} and
   * {@link #isFreezingAllowed()} return true.
   *
   * @throws IllegalArgumentException If the registry is frozen
   */
  private void testFrozen() throws IllegalArgumentException {
    if (isFrozen() && isFreezingAllowed()) {
      throw Util.newException("This registry is frozen and cannot be modified");
    }
  }

  /* ----------------------------- LISTENING ------------------------------ */

  /**
   * Sets the registry's listener.
   * <p>
   * A registry listener can only be set once, afterwards, any attempted
   * changes to a listener, will result in an exception being thrown
   *
   * @param listener The listener to set
   * @throws IllegalArgumentException If an indexer is already set
   */
  public void setListener(RegistryListener<V> listener)
      throws IllegalArgumentException
  {
    Objects.requireNonNull(listener);

    if (this.listener != null) {
      throw Util.newException("Registry listener already set!");
    }

    this.listener = listener;

    if (!isEmpty()) {
      byKey.values()
          .forEach(listener::onRegister);
    }
  }

  /* --------------------------- VALUE GETTERS ---------------------------- */

  /**
   * Gets an entry by its key
   * <p>
   * As with the {@link #register(String, Object, int)} method, this will check if the given key
   * contains a namespace, if it does, it is removed, this is because namespaces are a part of the
   * legacy {@link net.kyori.adventure.key.Key} based registry system
   *
   * @param key The key to get the entry of
   * @return An optional that's empty if this registry does not contain the given key, otherwise it
   * contains the entry associated with the given key
   * @throws IllegalArgumentException If the key failed the {@link Keys#ensureValid(String)} test
   */
  public @NotNull Optional<Holder<V>> getHolder(@Pattern(VALID_KEY_REGEX) String key)
      throws IllegalArgumentException {
    return Optional.ofNullable(byKey.get(
        Keys.ensureValid(removeNamespace(key)).intern()
    ));
  }

  /**
   * Gets a registry entry by its ID
   *
   * @param id The ID of the entry
   * @return An optional that's empty if this registry does not contain the given ID, otherwise it
   * contains the entry associated with the given ID
   */
  public @NotNull Optional<Holder<V>> getHolder(int id) {
    if (!contains(id)) {
      return Optional.empty();
    }

    return Optional.of(byId[id]);
  }

  /**
   * Gets an entry by its value.
   * <p>
   * This uses the {@link #byValue} hash map to find the value's entry. If the value's hash function
   * is not properly defined, then this function may not function properly either
   *
   * @param value The value to get the entry of
   * @return An optional that's empty if this registry does not contain the given value, otherwise
   * it contains the entry associated with the given value
   */
  public @NotNull Optional<Holder<V>> getHolderByValue(@NotNull V value) {
    return Optional.ofNullable(byValue.get(value));
  }

  /**
   * Gets value associated with the given key
   *
   * @param key The key to get the value of
   * @return An optional that has the key's value, or an empty optional, if the key this registry
   * doesn't contain the given key
   * @throws IllegalArgumentException If the key failed the {@link Keys#ensureValid(String)} test
   */
  public @NotNull Optional<V> get(@Pattern(VALID_KEY_REGEX) String key)
      throws IllegalArgumentException {
    return getHolder(key).map(Holder::getValue);
  }

  /**
   * Gets the value associated with the given ID
   *
   * @param id The ID to get the value of
   * @return An optional that has the key's value, or an empty optional, if the key this registry
   * doesn't contain the given key
   */
  public @NotNull Optional<V> get(int id) {
    return getHolder(id).map(Holder::getValue);
  }

  /**
   * Gets the value associated with the given key, or null
   *
   * @param key The key to get the value of
   * @return The value of the key, or null, if not present
   */
  public @Nullable V orNull(@Pattern(VALID_KEY_REGEX) String key) {
    return get(key).orElse(null);
  }

  /**
   * Gets the value associated with the given ID, or null
   *
   * @param id The ID to get the value of
   * @return The value of the ID, or null, if not present
   */
  public @Nullable V orNull(int id) {
    return get(id).orElse(null);
  }

  /**
   * Gets a value associated with a key or throws an {@link IllegalArgumentException} exception.
   *
   * @param key The key to get the value of
   * @return The found value
   * @throws IllegalArgumentException If the key did not have an associated value, or if key failed
   *                                  the {@link Keys#ensureValid(String)} test
   */
  public @NotNull V orThrow(@Pattern(VALID_KEY_REGEX) String key)
      throws IllegalArgumentException {
    return get(key).orElseThrow(() -> unknownKey(key));
  }

  /**
   * Gets a value associated with an ID or throws an {@link IllegalArgumentException} exception.
   *
   * @param id The ID to get the value of
   * @return The found value
   * @throws IllegalArgumentException If the ID did not have an associated value
   */
  public @NotNull V orThrow(int id) throws IllegalArgumentException {
    return get(id).orElseThrow(() -> Util.newException("Could not find value for ID %s", id));
  }

  /**
   * Gets the key associated with the given value
   *
   * @param value The value to get the key of
   * @return The optional containing the value's key, or an empty optional if the value could not be
   * found in the value map
   */
  public @NotNull Optional<String> getKey(@NotNull V value) {
    return getHolderByValue(value).map(Holder::getKey);
  }

  /**
   * Gets the ID associated with the given value
   *
   * @param value The value to get the ID of
   * @return The optional containing the value's ID, or an empty optional if the value could not be
   * found in the value map
   */
  public @NotNull OptionalInt getId(@NotNull V value) {
    return getHolderByValue(value)
        .map(vHolder -> OptionalInt.of(vHolder.getId()))
        .orElseGet(OptionalInt::empty);
  }

  /**
   * Tests if this registry contains the given entry. This method runs by testing if this registry
   * first contains the entry's ID by calling {@link #contains(int)} and then making sure the entry
   * at that ID is the same entry as the given one
   *
   * @param holder The entry to test
   * @return True, if this registry contains the given entry.
   */
  public boolean contains(Holder<V> holder) {
    if (!contains(holder.getId())) {
      return false;
    }

    return byId[holder.getId()].equals(holder);
  }

  /**
   * Tests if this registry contains the given.
   * <p>
   * If the given ID is less than 0 or larger than the ID lookup array, then this method returns
   * true, else it tests if the element at the given ID is not null.
   *
   * @param id The ID to test
   * @return True, if this registry contains the given ID, false otherwise
   */
  public boolean contains(int id) {
    return id >= 0 && id < byId.length && byId[id] != null;
  }

  /**
   * Tests if this registry contains the given key
   *
   * @param key The key to test
   * @return True, if this registry contains the given key, false otherwise
   */
  public boolean contains(String key) {
    return byKey.containsKey(removeNamespace(key));
  }

  /**
   * Tests if this registry contains the given value
   *
   * @param value The value to test
   * @return True, if this registry contains the given value, false otherwise
   */
  public boolean containsValue(V value) {
    return byValue.containsKey(value);
  }

  /**
   * Gets a random entry in this registry.
   * <p>
   * This method will make {@link #MAX_RANDOM_ATTEMPTS} amount of attempts to find a valid entry, if
   * no valid entry is found within that frame, then an empty optional is returned.
   * <p>
   * This method also uses a {@link IntSet} to ensure no duplicate entry IDs are selected.
   *
   * @param random    The random to use find an entry
   * @param predicate The predicate to apply to all entries to test if they're a valid return value
   * @return The optional containing the first random element that passed the given predicate, or no
   * element if no valid element was found within {@link #MAX_RANDOM_ATTEMPTS} attempts
   */
  public Optional<Holder<V>> getRandom(Random random, Predicate<Holder<V>> predicate) {
    final IntSet alreadyFound = new IntOpenHashSet();
    int resultID = -1;

    // Safeguard value to ensure this method never
    // runs into an infinite loop
    int safeGuard = MAX_RANDOM_ATTEMPTS;

    while (!contains(resultID)
        // Attempts to add ID, true -> ID not in set,
        // false -> ID has already been tried
        || !alreadyFound.add(resultID)

        || !predicate.test(byId[resultID])
    ) {
      resultID = random.nextInt(0, byId.length);

      // Ensure we never run into infinite loop
      --safeGuard;
      if (safeGuard < 0) {
        return Optional.empty();
      }
    }

    return Optional.of(byId[resultID]);
  }

  /* ------------------------ REGISTRY META INFO ------------------------- */

  /**
   * Tests if this registry is empty
   *
   * @return True, if the registry is empty, false otherwise
   */
  public boolean isEmpty() {
    return byKey.isEmpty();
  }

  /**
   * Gets the amount of entries registered within this registry
   *
   * @return The entry count
   */
  public int size() {
    return byKey.size();
  }

  /* ----------------------------- ITERATION ------------------------------ */

  /**
   * Gets an unmodifiable set of the string keys this registry has
   *
   * @return The registry's immutable key set
   */
  public @NotNull ObjectSet<String> keys() {
    return ObjectSets.unmodifiable(byKey.keySet());
  }

  /**
   * Gets an unmodifiable collection of all entries contained in this registry
   *
   * @return The registry's immutable entry set
   */
  public @NotNull ObjectCollection<Holder<V>> entries() {
    return ObjectCollections.unmodifiable(byKey.values());
  }

  /**
   * Creates a stream of all holders in this registry
   *
   * @return The holder stream
   */
  public Stream<Holder<V>> stream() {
    return byKey.values().stream();
  }

  /**
   * Gets an unmodifiable list of all values contained in this registry
   *
   * @return Immutable value list
   */
  public @NotNull ImmutableList<V> values() {
    var it = ArrayIterator.unmodifiable(byId);

    return ImmutableList.copyOf(
        Iterators.transform(it, Holder::getValue)
    );
  }

  /**
   * Gets an immutable iterator for iterating through the values contained in this registry
   *
   * @return immutable registry iterator
   */
  @Override
  public @NotNull ListIterator<V> iterator() {
    return new RegistryValueIterator();
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  /**
   * Reads a value from the give element.
   * <p>
   * This method will primary just return the key-associated value of the element, if the element is
   * a {@link JsonPrimitive} and a string, if it's an integer it treats that int as an ID and
   * attempts to get the value by that ID
   *
   * @param element The element to read from
   * @return The element's associated value in this registry, or an empty optional if a valid value
   * wasn't found
   */
  public @NotNull Optional<V> readJson(@NotNull JsonElement element) {
    return cast(element, JsonPrimitive.class)
        .flatMap(primitive -> get(primitive.getAsString()));
  }

  /**
   * Reads a JSON element as a key/ID and attempts to get a value matching that key/ID, if no value
   * is found then {@link #unknownKey(Object)} is thrown
   *
   * @param element The element to get the value from
   * @return The value
   * @throws IllegalArgumentException If a valid value for the given element couldn't be found
   */
  public @NotNull V readJsonOrThrow(@NotNull JsonElement element) throws IllegalArgumentException {
    return readJson(element).orElseThrow(() -> unknownKey(element));
  }

  /**
   * Writes the given value to JSON using the string key it's mapped to
   *
   * @param value The value to write
   * @return The JSON representation of the value, or an empty optional if a valid key, couldn't be
   * found
   */
  public @NotNull Optional<JsonPrimitive> writeJson(@NotNull V value) {
    return getKey(value).map(JsonPrimitive::new);
  }

  /**
   * Reads a value from the give tag.
   * <p>
   * This method will primary just return the key-associated value of the tag, if the tag is a
   * {@link StringTag}. But this method can also test if the tag is an {@link IntTag}, if it is,
   * then it presumes the tag acts as an ID tag and attempts to return the value associated with
   * that ID
   *
   * @param tag The tag to read from
   * @return The tag's associated value in this registry, or an empty optional if a valid value
   * wasn't found
   */
  public @NotNull Optional<V> readTag(@NotNull Tag tag) {
    return cast(tag, StringTag.class)
        .flatMap(tag1 -> get(tag1.getAsString()));
  }

  /**
   * Reads an NBT tag as a key/ID and attempts to get a value matching that key/ID, if no value is
   * found then {@link #unknownKey(Object)} is thrown
   *
   * @param tag The tag to get the value from
   * @return The value
   * @throws IllegalArgumentException If a valid value for the given tag couldn't be found
   */
  public @NotNull V readTagOrThrow(@NotNull Tag tag) throws IllegalArgumentException {
    return readTag(tag).orElseThrow(() -> unknownKey(tag.getAsString()));
  }

  /**
   * Writes the key associated with the given value to NBT
   *
   * @param value The value to write
   * @return The NBT of the value, if the value is contained in this registry
   */
  public @NotNull Optional<StringTag> writeTag(@NotNull V value) {
    return getKey(value).map(StringTag::valueOf);
  }

  /* ----------------------------- OBJECT OVERRIDES ------------------------------ */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Registry<?> registry)) {
      return false;
    }

    return registry.getRegistryId() == getRegistryId();
  }

  @Override
  public int hashCode() {
    return getRegistryId();
  }

  @Override
  public String toString() {
    return byKey.toString();
  }

  /* ----------------------------- UTILITY ------------------------------ */

  /**
   * Used to shorten the amount of characters I'd have to write for the 2 read methods in this
   * class
   *
   * @param val   The object to cast
   * @param clazz The class to cast it to
   * @param <T>   The cast type
   * @return An optional which contains the given type if cast successfully, otherwise empty
   */
  static <T> Optional<T> cast(Object val, Class<T> clazz) {
    if (!clazz.isInstance(val)) {
      return Optional.empty();
    }

    return Optional.of(clazz.cast(val));
  }

  /**
   * Creates an exception which states the given key is unknown
   *
   * @param key The key that's unknown
   * @return The created exception
   */
  public static IllegalArgumentException unknownKey(Object key) {
    return Util.newException("Couldn't find value for key: '%s'", key);
  }

  /**
   * Removes the namespace on the given key.
   * <p>
   * If the given key does not have a namespace, then the input is returned, otherwise, the input is
   * split at the ':' character, then it is ensured that the area after the namespace is not blank,
   * if it is a {@link IllegalArgumentException} will be thrown, if that's not thrown, then the
   * cropped input is returned
   *
   * @param s The key to remove the namespace of
   * @return The key without a namespace
   * @throws IllegalArgumentException If the given string ended with a ':', as that means the key's
   *                                  actual value was blank
   */
  public static @NotNull String removeNamespace(@NotNull String s) throws IllegalArgumentException {
    if (!s.contains(":")) {
      return s;
    }

    var after = s.split(":")[1];

    if (after.isBlank()) {
      throw Util.newException("Invalid key: '%s', blank after namespace", s);
    }

    return after;
  }

  /* ----------------------------- ITERATOR SUB CLASS ------------------------------ */

  /**
   * Immutable implementation of {@link ListIterator} for iterating through a registry in the order
   * of it's ID array.
   *
   * @see AbstractListIterator
   */
  public class RegistryValueIterator extends AbstractListIterator<V> {

    @Override
    protected final void add(int location, V v) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected final void set(int location, V v) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected final V get(int location) {
      var holder = byId[location];

      if (holder == null) {
        return null;
      }

      return holder.getValue();
    }

    @Override
    protected final void remove(int location) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected int size() {
      return Registry.this.size();
    }
  }
}