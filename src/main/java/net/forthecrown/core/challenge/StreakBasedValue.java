package net.forthecrown.core.challenge;

import com.google.gson.JsonElement;
import net.forthecrown.utils.io.JsonWrapper;
import org.spongepowered.math.GenericMath;

/**
 * A container for a value that's generated based on a
 * player's challenge completion streak.
 */
@FunctionalInterface
public interface StreakBasedValue {
    /** Fixed value constant that returns 0 */
    StreakBasedValue EMPTY = streak -> 0;

    /** Fixed value constant that returns 1 */
    StreakBasedValue ONE = streak -> 1;

    /**
     * Gets the value of this container
     * @param streak A player's streak
     * @return The calculated value
     */
    float getValue(int streak);

    /**
     * Gets the value of this container, rounded down to an integer
     * @param streak A player's streak
     * @return The calculated, floored, value
     */
    default int getInt(int streak) {
        return GenericMath.floor(getValue(streak));
    }

    /**
     * Creates a fixed streak-based container that always returns the input
     * @param value The value to return
     * @return The created container
     */
    static StreakBasedValue fixed(float value) {
        if (value < 1) {
            return EMPTY;
        }

        return streak -> value;
    }

    /**
     * Creates a streak based value that uses the given array to fetch values,
     * treating the streak value that it's given as an index for said array and
     * clamping the streak value to the array's maximum index.
     *
     * @param values The values array to use
     * @return The created container
     */
    static StreakBasedValue arrayBased(float[] values) {
        return streak -> {
            int index = GenericMath.clamp(streak, 0, values.length - 1);
            return values[index];
        };
    }

    /**
     * Creates a streak based value that scales using the given scalar and base,
     * the formula this uses is very simple: <code>result = base * (streak * scalar)</code>.
     * The streak that's inputted into the formula is also
     * {@link Math#max(float, float)}-ed, so it doesn't drop below 1.
     *
     * @param base The base value
     * @param scalar The scaling value
     *
     * @return The created container
     */
    static StreakBasedValue scalar(float base, float scalar) {
        return streak -> {
            float streakF = Math.max(1.0F, streak);
            return base * (streakF * scalar);
        };
    }

    /**
     * Reads a streak based value from a JSON element, and returns
     * {@link #EMPTY} if it fails to read or if the input is null.
     * @param element The element to read from
     * @return The read value
     * @see #read(JsonElement, StreakBasedValue)
     */
    static StreakBasedValue read(JsonElement element) {
        return read(element, EMPTY);
    }

    /**
     * Reads a streak based value from the given element and returns the given
     * default value if it fails to read.
     * <p>
     * 'Fails to read' occurs when the given input is null or is malformed in
     * some way. If the input is an array-based value, then default is returned
     * when the array's size is less than 0, and if it's scalar-based value,
     * then the default is returned when either the <code>base</code> or
     * <code>scalar</code> are 0.
     *
     * @param element The element to read from
     * @param def The default value to return
     *
     * @return The read value
     */
    static StreakBasedValue read(JsonElement element, StreakBasedValue def) {
        if (element == null || element.isJsonNull()) {
            return def;
        }

        // Fixed
        if (element.isJsonPrimitive()) {
            return fixed(element.getAsFloat());
        }

        // Array-based
        if (element.isJsonArray()) {
            var arr = element.getAsJsonArray();
            float[] values = new float[arr.size()];

            for (int i = 0; i < arr.size(); i++) {
                values[i] = arr.get(i).getAsFloat();
            }

            if (values.length < 1) {
                return def;
            }

            return arrayBased(values);
        }

        // Scalar-based
        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
        float base = json.getFloat("base", 1.0F);
        float scalar = json.getFloat("scalar", 1.0F);

        if (base <= 0 || scalar <= 0) {
            return def;
        }

        return scalar(base, scalar);
    }
}