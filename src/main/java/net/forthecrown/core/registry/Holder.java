package net.forthecrown.core.registry;

import java.util.Comparator;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode.Exclude;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * A single entry within a Registry.
 * <p>
 * Holder's are immutable, because once registered, they do not change unless
 * they are unregistered.
 * <p>
 * Also, I will not lie, the reason this is called a 'Holder' instead of
 * something like 'RegistryEntry' is because I copied the vanilla registries
 * naming convention, which was named Holder for a reason, while this is not,
 * this is just an immutable entry. Also, 'Holder' is a lot less characters lol
 *
 * @param <V> The entry's type
 * @see Registry
 */
@Data
public final class Holder<V> {

  /**
   * The holder's key
   */
  private final @NotNull String key;

  /**
   * The ID of the holder, acts as the index of the holder's index in the type
   * lookup array
   */
  private final int id;

  /**
   * The holder's value
   */
  @Exclude
  private final @NotNull V value;

  /**
   * The registry this holder is bound to
   */
  @ToString.Exclude
  @Exclude
  @Setter(AccessLevel.PACKAGE)
  Registry<V> registry;

  /* ---------------------------- COMPARATORS ----------------------------- */

  public static <T> Comparator<Holder<T>> comparingByKey() {
    return Comparator.comparing(Holder::getKey);
  }

  public static <T> Comparator<Holder<T>> comparingById() {
    return Comparator.comparing(Holder::getId);
  }

  public static <T> Comparator<Holder<T>> comparingByValue(
      Comparator<T> comparator
  ) {
    return (o1, o2) -> {
      return comparator.compare(o1.getValue(), o2.getValue());
    };
  }

  public static <T extends Comparable<T>> Comparator<Holder<T>> comparingByValue() {
    return Comparator.comparing(Holder::getValue);
  }
}