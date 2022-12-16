package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.CosmeticType;
import net.forthecrown.user.ComponentType;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.io.JsonWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Holds the active and available data for
 * the cosmetics of a user.
 */
public class CosmeticData extends UserComponent {
    /* ----------------------------- CONSTANTS ------------------------------ */

    /**
     * The JSON key of an active cosmetic within a
     * single {@link Entry}
     * @see Entry
     */
    public static final String KEY_ACTIVE = "active";

    /**
     * The JSON key of the active cosmetic list within
     * a single {@link Entry}
     * @see Entry
     */
    public static final String KEY_AVAILABLE = "available";

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /**
     * Entry array where index corresponds to the {@link Entry#getType()}'s
     * {@link CosmeticType#getId()}.
     * <p>
     * Final and not resized due to the fact that there aren't a lot of
     * cosmetics
     * @see Entry
     */
    private final Entry[] entries = new Entry[Registries.COSMETIC.size()];

    /* ----------------------------- CONSTRUCTOR ------------------------------ */

    public CosmeticData(User user, ComponentType<CosmeticData> type) {
        super(user, type);
    }

    /* ----------------------------- METHODS ------------------------------ */

    /**
     * Checks if the user has the given effect
     * @param effect The effect to check for
     * @return True, if the user has the given effect, false otherwise
     */
    public boolean contains(Cosmetic effect) {
        var entry = entries[effect.getType().getId()];

        if (entry == null) {
            return false;
        }

        return entry.contains(effect);
    }

    /**
     * Removes the given cosmetic from this user
     * @param effect The effect to remove
     * @return True, if this method call changed
     *         this data, false otherwise
     */
    public boolean remove(Cosmetic effect) {
        if (!contains(effect)) {
            return false;
        }

        return entries[effect.getType().getId()].remove(effect);
    }

    /**
     * Adds the given effect to this user
     * @param effect The effect to add
     * @return True, if this method call changed
     *         this data, false otherwise
     */
    public boolean add(Cosmetic effect) {
        var entry = getEntry(effect.getType());
        return entry.add(effect);
    }

    /**
     * Gets the active cosmetic for the given type
     * @param type The type to get the cosmetic of
     * @param <T> The cosmetic type
     * @return The active cosmetic, null, if the user
     *         doesn't have an active cosmetic of the
     *         given type
     */
    public <T extends Cosmetic> T get(CosmeticType<T> type) {
        var entry = entries[type.getId()];

        if (entry == null || entry.getActive() == null) {
            return null;
        }

        return (T) entry.getActive();
    }

    /**
     * Sets the active type of cosmetic
     * @param type The cosmetic's type
     * @param val The active cosmetic, null, to clear the active cosmetic
     * @param <T> The cosmetic's type
     */
    public <T extends Cosmetic> void set(CosmeticType<T> type, @Nullable T val) {
        var e = getEntry(type);
        e.setActive(val);
    }

    @NotNull
    private <T extends Cosmetic> CosmeticData.Entry<T> getEntry(CosmeticType<T> type) {
        Entry<T> entry = entries[type.getId()];

        if (entry == null) {
            entry = new Entry<>(type);
            entries[type.getId()] = entry;
        }

        return entry;
    }

    /**
     * Gets all available cosmetics of the given
     * type this user has
     * @param type The type to query
     * @param <T> The cosmetic's type
     *
     * @return All available cosmetics of the given type
     *         the user has, or {@link Collections#emptyList()}
     *         if they have no availble cosmetics.
     */
    public <T extends Cosmetic> Collection<T> getAvailable(CosmeticType<T> type) {
        Entry<T> entry = entries[type.getId()];

        if (entry == null || entry.isAvailableEmpty()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableSet(entry.getAvailable());
    }

    /**
     * Clears all available cosmetics this user
     * has under the given type
     * @param type The type to clear.
     */
    public void clear(CosmeticType type) {
        var entry = entries[type.getId()];

        if (entry == null || entry.isAvailableEmpty()) {
            return;
        }

        entry.available.clear();
    }

    /**
     * Clears all active and available cosmetics
     * in this user's data
     */
    public void clear() {
        Arrays.fill(entries, null);
    }

    /**
     * Tests if the cosmetic data is empty
     * @return True, if this data instance has no active
     *         or available effects, false otherwise
     */
    public boolean isEmpty() {
        var it = ArrayIterator.unmodifiable(entries);

        while (it.hasNext()) {
            if (!it.next().isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public <T extends Cosmetic> boolean isUnset(CosmeticType<T> type) {
        var entry = entries[type.getId()];
        return entry == null || entry.getActive() == null;
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    @Override
    public void deserialize(JsonElement element) {
        clear();

        if (element == null) {
            return;
        }

        var json = JsonWrapper.wrap(element.getAsJsonObject());

        // Loop through all cosmetic types and test if the JSON has the
        // data for that type, if not, move onto to next type, if it does
        // deserialize the data for it and add it to this cosmetics data
        for (var t: Registries.COSMETIC) {
            if (!json.has(t.getName())) {
                continue;
            }

            var entry = Entry.deserialize(json.get(t.getName()), t);
            entries[t.getId()] = entry;
        }
    }

    @Override
    public JsonObject serialize() {
        JsonWrapper json = JsonWrapper.create();

        // Loop through map, if entry empty, skip it, else
        // serialize it, ez pz lemon squezy
        var it = ArrayIterator.unmodifiable(entries);
        while (it.hasNext()) {
            var entry = it.next();

            if (entry.isEmpty()) {
                continue;
            }

            json.add(entry.getType().getName(), entry.serialize());
        }

        return json.nullIfEmpty();
    }

    /* ----------------------------- SUB CLASSES ------------------------------ */

    /**
     * A single type's entry in a user's cosmetic data.
     * @param <T> The cosmetic's type
     */
    @Getter
    @RequiredArgsConstructor
    private static class Entry<T extends Cosmetic> {
        /**
         * The type this entry belongs to
         */
        private final CosmeticType<T> type;

        /**
         * The currently active cosmetic
         */
        @Setter
        private T active;

        /**
         * A set of available cosmetics
         * <p>
         * This set may be null, use {@link #isAvailableEmpty()}
         * to test if any availble effects exist and that this
         * list is not null
         */
        @Nullable
        private Set<T> available;

        /**
         * Tests if this entry contains the given
         * cosmetic
         *
         * @param effect The cosmetic to look for
         * @return True, if contained, false otherwise
         */
        public boolean contains(T effect) {
            if (isAvailableEmpty()) {
                return false;
            }

            return available.contains(effect);
        }

        /**
         * Adds the given cosmetic to this
         * entry
         *
         * @param effect The cosmetic to add
         * @return {@link Collection#add(Object)}
         */
        public boolean add(T effect) {
            if (available == null) {
                available = new ObjectOpenHashSet<>();
            }

            return available.add(effect);
        }

        /**
         * Removes the given cosmetic
         * from this entry
         * @param effect The cosmetic to remove
         * @return {@link Collection#remove(Object)}
         */
        public boolean remove(T effect) {
            if (isAvailableEmpty()) {
                return false;
            }

            return available.remove(effect);
        }

        /**
         * Tests if this entry is empty, meaning
         * that the {@link #active} is unset and
         * that the {@link #available} set is either
         * empty or null.
         *
         * @return True, if this entry is empty, false otherwise
         */
        public boolean isEmpty() {
            return active == null && isAvailableEmpty();
        }

        /**
         * Tests if the {@link #available} set is empty
         * or null.
         *
         * @return True, if the {@link #available} set is
         *         empty or null
         */
        public boolean isAvailableEmpty() {
            return available == null || available.isEmpty();
        }

        /**
         * Serializes the entry into a JSON object with either
         * 1 or 2 entries.
         * <p>
         * If {@link #isEmpty()} returns true, this returns null.
         * <p>
         * The 2 entries the resulting object may contain are
         * the available cosmetics list, under the {@link #KEY_AVAILABLE}
         * key and the active cosmetic, under the {@link #KEY_ACTIVE}
         * key. Either of these may not be present if their values
         * are either unset, null or empty.
         *
         * @return The serialized represenation of this entry
         */
        public JsonElement serialize() {
            if (isEmpty()) {
                return null;
            }

            JsonWrapper json = JsonWrapper.create();

            if (getActive() != null) {
                json.add(KEY_ACTIVE, getActive().getSerialId());
            }

            if (!isAvailableEmpty()) {
                json.addList(KEY_AVAILABLE, getAvailable(), t -> new JsonPrimitive(t.getSerialId()));
            }

            return json.getSource();
        }

        /**
         * Deserializes an entry with the given type
         * from the given JSON element.
         * <p>
         * @see #serialize() for the serialization schema
         * @param element The element to deserialize from
         * @param type The cosmetic type
         * @param <T> The cosmetic's type
         * @return The deserialized entry
         */
        public static <T extends Cosmetic> Entry<T> deserialize(JsonElement element,
                                                                CosmeticType<T> type
        ) {
            JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
            var result = new Entry<>(type);

            if (json.has(KEY_ACTIVE)) {
                var key = json.getString(KEY_ACTIVE);
                result.setActive(type.getEffects().orThrow(key));
            }

            if (json.has(KEY_AVAILABLE)) {
                result.available = new ObjectOpenHashSet<>(
                        json.getList(KEY_AVAILABLE, element1 -> type.getEffects()
                                .readJsonOrThrow(element1)
                        )
                );
            }

            return result;
        }
    }
}