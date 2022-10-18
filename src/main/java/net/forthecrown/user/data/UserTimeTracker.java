package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import net.forthecrown.core.Crown;
import net.forthecrown.core.registry.Registries;
import net.forthecrown.core.registry.Registry;
import net.forthecrown.user.ComponentType;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;

import java.util.Arrays;

/**
 * A {@link UserComponent} that tracks time stamps,
 * specified by {@link TimeField}s.
 */
public class UserTimeTracker extends UserComponent {
    /**
     * A registry of all {@link TimeField} objects to allow for easy lookups
     */
    public static final Registry<TimeField> TIME_FIELDS = Registries.ofEnum(TimeField.class);

    /**
     * The value returned by {@link #get(TimeField)} if the
     * given field has not been set.
     */
    public static final long UNSET = -1L;

    /**
     * The array of time stamp values
     */
    private final long[] stamps;

    public UserTimeTracker(User user, ComponentType<UserTimeTracker> type) {
        super(user, type);

        // Since there aren't many timestamps,
        // initialize array to full size
        stamps = new long[TIME_FIELDS.size()];

        // Call clear just to fill the array
        // with UNSET values
        clear();
    }

    /**
     * Sets the given field to the given value
     * @param field The field to set
     * @param val the value to set the field to
     */
    public void set(TimeField field, long val) {
        stamps[field.ordinal()] = val;
    }

    /**
     * Sets the given field to {@link System#currentTimeMillis()}
     * @param field The field to set
     */
    public void setCurrent(TimeField field) {
        set(field, System.currentTimeMillis());
    }

    /**
     * Adds to the given field
     * <p>
     * If the given field is not set when
     * this is called, the field is simply
     * set to the given value
     *
     * @param field The field to add to
     * @param val the amount to add
     */
    public void add(TimeField field, long val) {
        if (isSet(field)) {
            set(field, get(field) + val);
        } else {
            set(field, val);
        }
    }

    /**
     * Removes the given field.
     * <p>
     * Functionally identical to <code>set(field, UNSET)</code>
     * @param field The field to remove
     */
    public void remove(TimeField field) {
        set(field, UNSET);
    }

    /**
     * Gets the value of the field
     * @param field The field to get
     * @return The field's value, returns
     *         {@link #UNSET} if the field
     *         has not been set
     */
    public long get(TimeField field) {
        return stamps[field.ordinal()];
    }

    /**
     * Tests if the given field has been set
     * @param field The field to test
     * @return True, if the field's value is anything
     *         other than {@link #UNSET}
     */
    public boolean isSet(TimeField field) {
        return get(field) != UNSET;
    }

    /**
     * Clears the time stamps
     * <p>
     * Functionally, this just fills
     * the time stamp array with
     * {@link #UNSET} values
     */
    public void clear() {
        Arrays.fill(stamps, UNSET);
    }

    @Override
    public JsonElement serialize() {
        var obj = JsonWrapper.create();

        // Loop through all time stamps
        for (int i = 0; i < stamps.length; i++) {
            // Get time stamp by its ID
            var field = TIME_FIELDS.orNull(i);

            // Don't serialize transient time stamps
            if (!field.isSerialized()) {
                continue;
            }

            // Don't serialize unset time stamps
            if (!isSet(field)) {
                continue;
            }

            // Serialize by date
            obj.addTimeStamp(field.getKey(), get(field));
        }

        return obj.nullIfEmpty();
    }

    @Override
    public void deserialize(JsonElement element) {
        clear();

        if (element == null) {
            return;
        }

        var obj = element.getAsJsonObject();

        // Loop through object entries
        for (var e: obj.entrySet()) {
            // Find field
            var field = TIME_FIELDS.get(e.getKey());

            // Couldn't find field
            if (field.isEmpty()) {
                Crown.logger().warn("Found unknown time field: '{}'", e.getKey());
                continue;
            }

            // Deserialize value, if it's a string, it's a date object, if it's a long
            // it's a serialized time stamp, serialize accordingly
            var jsonVal = e.getValue().getAsJsonPrimitive();
            var val = jsonVal.isString() ? JsonUtils.readDate(jsonVal).getTime() : jsonVal.getAsLong();

            // Set field's value
            set(field.get(), val);
        }
    }
}