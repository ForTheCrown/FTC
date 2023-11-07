package net.forthecrown.challenges;

import com.google.gson.JsonElement;
import net.forthecrown.utils.io.JsonWrapper;
import org.spongepowered.math.GenericMath;

/**
 * A container for a value that's generated based on a player's challenge
 * completion streak.
 * <p>
 * There curretly exist 3 types of streak based values, they are as follows:
 *
 * <h3>Fixed</h3>
 * Simple fixed values that always return the same value regardless of a
 * player's streak.
 *
 * <h3>Scalar</h3>
 * Returns a {@code baseValue} scaled by a {@code scalar} and the player's
 * streak. The formula scalars use such:
 * {@code result = baseValue * (Math.max(1, streak) * scalar)}
 *
 * <h3>Daph scalar</h3>
 * Named after daphlipan. Modified version of scalar type values. Instead of the
 * above shown formula for calculation, they use the following:
 * {@code result = base * (1 + streak * 0.01)}
 */
@FunctionalInterface
public interface StreakBasedValue {

  /**
   * Fixed value constant that returns 0
   */
  StreakBasedValue EMPTY = streak -> 0;

  /**
   * Fixed value constant that returns 1
   */
  StreakBasedValue ONE = streak -> 1;

  /**
   * Gets the value of this container
   *
   * @param streak A player's streak
   * @return The calculated value
   */
  float getValue(int streak);

  /**
   * Gets the value of this container, rounded down to an integer
   *
   * @param streak A player's streak
   * @return The calculated, floored, value
   */
  default int getInt(int streak) {
    return GenericMath.floor(getValue(streak));
  }

  /**
   * Creates a fixed streak-based container that always returns the input
   *
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
   * Creates a streak based value that scales using the given scalar and base, the formula this uses
   * is very simple: <code>result = base * (streak * scalar)</code>. The streak that's inputted into
   * the formula is also {@link Math#max(float, float)}-ed, so it doesn't drop below 1.
   *
   * @param base   The base value
   * @param scalar The scaling value
   * @return The created container
   */
  static StreakBasedValue scalar(float base, float scalar) {
    return streak -> {
      float streakF = Math.max(1.0F, streak);
      return base * (streakF * scalar);
    };
  }

  static StreakBasedValue daphScalar(float base) {
    return streak -> base * (1.0F + Math.max(1.0F, streak) * 0.01F);
  }

  /**
   * Reads a streak based value from a JSON element, and returns {@link #EMPTY} if it fails to read
   * or if the input is null.
   *
   * @param element The element to read from
   * @return The read value
   * @see #read(JsonElement, StreakBasedValue)
   */
  static StreakBasedValue read(JsonElement element) {
    return read(element, EMPTY);
  }

  /**
   * Reads a streak based value from the given element and returns the given default value if it
   * fails to read.
   * <p>
   * 'Fails to read' occurs when the given input is null or is malformed in some way. If the input
   * is an array-based value, then default is returned when the array's size is less than 0, and if
   * it's scalar-based value, then the default is returned when either the <code>base</code> or
   * <code>scalar</code> are 0.
   *
   * @param element The element to read from
   * @param def     The default value to return
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

    // Scalar-based
    JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

    if (json.has("daphScalar")) {
      return daphScalar(json.getFloat("daphScalar"));
    }

    float base = json.getFloat("base", 1.0F);
    float scalar = json.getFloat("scalar", 0.0F);

    if (base <= 0 || scalar <= 0) {
      return def;
    }

    return scalar(base, scalar);
  }
}