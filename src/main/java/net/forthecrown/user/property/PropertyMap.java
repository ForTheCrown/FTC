package net.forthecrown.user.property;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.forthecrown.core.FTC;
import net.forthecrown.user.ComponentType;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.ArrayIterator;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A map of {@link UserProperty} properties
 */
public class PropertyMap extends UserComponent {
    /**
     * The backing object array that stores property values.
     * What index corresponds to what property is determined
     * by {@link UserProperty#getIndex()}, which is generated
     * during runtime.
     * <p>
     * Because the indexes only affect the runtime value array,
     * the order the properties are initialized in shouldn't
     * matter in the slightest as all it determines is what
     * order this array is in
     */
    private Object[] values = ArrayUtils.EMPTY_OBJECT_ARRAY;

    /**
     * Creates the property map for a given user
     * @param user The user to construct the map for
     */
    public PropertyMap(User user, ComponentType<PropertyMap> type) {
        super(user, type);
    }

    /**
     * Gets the value of the property.
     * @param property The property to get the value of
     * @param <T> The property's type
     * @return The set value of the property, or the property's default value
     */
    @NotNull
    public <T> T get(@NotNull UserProperty<T> property) {
        if (!contains(property)) {
            return property.getDefaultValue();
        }

        return (T) values[property.getIndex()];
    }

    /**
     * Sets the value of the property to the given value
     * @param property The property to set the value of
     * @param value The value of the property
     * @param <T> The property's type
     *
     * @return True, if property was changed, false otherwise.
     *         If false is returned, it means you attempted to
     *         set the value of the property to the value it
     *         already had
     */
    public <T> boolean set(@NotNull UserProperty<T> property, @Nullable T value) {
        return _set(property, value, true);
    }

    // Internal set method to allow for value update callbacks to be ignored,
    // That is required during deserialization
    private <T> boolean _set(@NotNull UserProperty<T> property, @Nullable T value, boolean update) {
        var existing = get(property);

        // If value is already set to this, stop
        if (existing.equals(value)) {
            return false;
        }

        // If we're unsetting the value or reverting it back
        // to the default, remove it, we don't need to store it then
        if (value == null || property.getDefaultValue().equals(value)) {
            remove(property);
        } else {
            int index = property.getIndex();
            values = ObjectArrays.ensureCapacity(values, index + 1);

            // Set the value
            values[index] = value;
        }

        // If user is online, update
        if (user.isOnline() && update) {
            property.onUpdate(user);
        }

        return true;
    }

    /**
     * Flips the value of the given boolean property
     * @param property The property to flip
     * @return the new value of the property
     */
    public boolean flip(@NotNull BoolProperty property) {
        var state = !get(property);
        set(property, state);

        return state;
    }

    /**
     * Removes the value of the given property
     * @param property The property to remove
     * @return True, if the property was contained in
     *         this map before removal, false otherwise
     */
    public boolean remove(@NotNull UserProperty property) {
        if (!contains(property)) {
            return false;
        }

        values[property.getIndex()] = null;
        return true;
    }

    /**
     * Tests if this map contains the given property
     * @param property The property to test
     * @return True, if the given property has a set
     *         value in this map.
     */
    public boolean contains(@NotNull UserProperty property) {
        if (values.length <= property.getIndex()) {
            return false;
        }

        var val = values[property.getIndex()];
        return val != null && !property.getDefaultValue().equals(val);
    }

    /**
     * Tests if this array is empty
     * @return True, if the array is empty, false otherwise
     */
    public boolean isEmpty() {
        return !ArrayIterator.unmodifiable(values).hasNext();
    }

    /**
     * Clears the map
     */
    public void clear() {
        values = ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    /* ----------------------------- SERIALIZATION ------------------------------ */

    @Override
    public void deserialize(JsonElement element) {
        clear();

        // Given serialization element is null, stop
        if (element == null) {
            return;
        }

        // Loop through object entries
        var obj = element.getAsJsonObject();
        for (var e: obj.entrySet()) {
            // Get property by entry name
            UserProperty property = Properties.USER_PROPERTIES.orNull(e.getKey());

            // Test the property isn't null
            if (property == null) {
                FTC.getLogger().warn("Found unknown user property: '{}', skipping", e.getKey());
                continue;
            }

            // Deserialize the value and set it
            _set(property, property.deserialize(e.getValue()), false);
        }
    }

    @Override
    public JsonElement serialize() {
        var json = new JsonObject();

        // Loop through each index of the value array
        // Run the loop backwards so that during deserialization
        // the property with the greatest ID is set first,
        // meaning the value array is resized to the perfect
        // length with the first read entry
        for (int i = values.length - 1; i >= 0; i--) {
            // Get the property by the current index
            UserProperty property = Properties.USER_PROPERTIES.orNull(i);

            // contains() check here to test if the
            // set value is null or the default, if
            // it is, then don't serialize
            if (!contains(property)) {
                continue;
            }

            // Serialize and add to JSON
            var val = get(property);
            json.add(property.getKey(), property.serialize(val));
        }

        return json.size() <= 0 ? null : json;
    }
}