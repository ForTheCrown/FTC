package net.forthecrown.core.registry;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.HashSet;
import java.util.regex.Pattern;
import net.forthecrown.utils.TransformingSet;
import org.intellij.lang.annotations.Language;

/**
 * Class that provides registry constants for some features of FTC and also provides factory
 * methods.
 * <p>
 * Use {@link #newRegistry()} and {@link #newFreezable()} to create registries.
 * {@link #ofEnum(Class)} will create a registry of all enum constants in a given class and try to
 * either find a key from the enums, if they implemement {@link FtcKeyed} or simply use enum's name
 * in lowercase form
 *
 * @see #newRegistry()
 * @see #newFreezable()
 * @see #ofEnum(Class)
 * @see Registry
 */
public final class Registries {
  private Registries() {}

  /**
   * The regex for determining if a {@link Registry} key is valid
   */
  @Language("RegExp")
  public static final
  String VALID_KEY_REGEX = "[a-zA-Z0-9+\\-/._]+";

  /**
   * Pattern made with {@link #VALID_KEY_REGEX}
   */
  public static final Pattern VALID_KEY = Pattern.compile(VALID_KEY_REGEX);

  /**
   * Creates a registry which CANNOT be frozen.
   * <p>
   * Calling {@link Registry#freeze()} on a registry created with this method will throw an
   * exception
   *
   * @param <V> The registry's type
   * @return The created registry
   * @see Registry#freeze()
   */
  public static <V> Registry<V> newRegistry() {
    return new Registry<>(false);
  }

  /**
   * Creates a registry which can be frozen.
   * <p>
   * Calling {@link Registry#freeze()} won't throw any errors
   *
   * @param <V> The registry's type
   * @return The created registry
   * @see Registry#freeze()
   */
  public static <V> Registry<V> newFreezable() {
    return new Registry<>(true);
  }

  /**
   * Creates a frozen registry out of all enum constants in the given class.
   * <p>
   * This will loop through each enum constant and test if its an instance of {@link FtcKeyed}, if
   * it is, it uses the result of {@link FtcKeyed#getKey()} as the enum's key, else it just uses
   * {@link Enum#name()}. Each enum is also registered with its {@link Enum#ordinal()} as its ID
   *
   * @param enumClass The class to turn into a registry
   * @param <E>       The enum's type
   * @return The created frozen registry
   */
  public static <E extends Enum<E>> Registry<E> ofEnum(Class<E> enumClass) {
    Registry<E> registry = newFreezable();

    for (var e : enumClass.getEnumConstants()) {
      String key;

      if (e instanceof FtcKeyed keyed) {
        key = keyed.getKey();
      } else {
        key = e.name().toLowerCase();
      }

      registry.register(key, e, e.ordinal());
    }

    registry.freeze();
    return registry;
  }

  /**
   * Ensures a given key matches the {@link Registries#VALID_KEY_REGEX} regex pattern
   *
   * @param s The string to test
   * @return The input string
   * @throws IllegalArgumentException If the given key was not valid
   */
  public static @org.intellij.lang.annotations.Pattern(VALID_KEY_REGEX) String ensureValidKey(
      String s
  ) throws IllegalArgumentException {
    Preconditions.checkArgument(isValidKey(s),
        "Invalid key: '%s' did not match '%s' pattern",
        s, VALID_KEY_REGEX
    );

    return s;
  }

  /**
   * Tests if the entire given string matches the {@link Registries#VALID_KEY_REGEX} regex pattern
   *
   * @param s The string to test
   * @return True, if the entire string matches, false otherwise
   */
  public static boolean isValidKey(String s) {
    return VALID_KEY.matcher(s).matches();
  }

  /**
   * Tests if a given character matches the {@link Registries#VALID_KEY_REGEX} regex pattern and is valid
   *
   * @param c The character to test
   * @return True, if the character matches, false otherwise
   */
  public static boolean isValidKeyChar(char c) {
    return isValidKey("" + c);
  }

  public static <V> TransformingSet<String, V> keyBackedSet(
      Registry<V> registry
  ) {
    return new TransformingSet<>(
        new HashSet<>(),
        registry::orNull,
        v -> registry.getKey(v).orElseThrow()
    );
  }

  public static <V> TransformingSet<Integer, V> idBackedSet(
      Registry<V> registry
  ) {
    return new TransformingSet<>(
        new IntOpenHashSet(),
        registry::orNull,
        v -> registry.getId(v).orElseThrow()
    );
  }
}