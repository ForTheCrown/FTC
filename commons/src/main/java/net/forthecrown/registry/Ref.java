package net.forthecrown.registry;

import java.util.Optional;
import org.jetbrains.annotations.Nullable;

 /**
 * A reference to an entry within a registry
 *
 * <p>
 * Use example:
 * <pre><code>
 * Registry&lt;UserRank&gt; registry = // ...
 *
 * KeyRef&lt;UserRank&gt; ref = Ref.key("default");
 *
 * Optional&lt;UserRank&gt; value = ref.get(registry);
 * String key = ref.getKey();
 * </code></pre>
 *
 * @param <V> Registry's type
 */
public interface Ref<V> {

   /**
    * Empty reference that always returns {@link Optional#empty()}
    */
  Ref EMPTY = new EmptyRef();

  /**
   * Creates a reference to a registry value via its numerical ID
   * @param id Entry ID
   * @return Created reference
   */
  static <V> IdRef<V> id(int id) {
    return new IdRef<>(id);
  }

  /**
   * Creates a reference to a registry value via its string key
   * @param name Entry key
   * @return Created reference
   */
  static <V> KeyRef<V> key(String name) {
    return new KeyRef<>(name);
  }

  /**
   * Gets the empty reference instance
   * @return Empty reference
   */
  static <V> Ref<V> empty() {
    return EMPTY;
  }

  /**
   * Gets the referenced value
   *
   * @param registry Registry to access the value from
   * @return Referenced value, empty optional, if the value pointed to by this
   *         reference, was not found
   */
  default Optional<V> get(Registry<V> registry) {
    return getHolder(registry).map(Holder::getValue);
  }

   /**
    * Gets the value's registry entry
    * @param registry Registry to access the value from
    * @return Holder value optional
    */
  Optional<Holder<V>> getHolder(Registry<V> registry);

   /**
    * Gets the referenced value, or {@code null}
    * @return Referenced value, or {@code null}, if value was not found
    */
  default @Nullable V orNull(Registry<V> registry) {
    return get(registry).orElse(null);
  }

  V orElseThrow(Registry<V> registry) throws IllegalArgumentException;

  default V orElse(Registry<V> registry, V def) {
    return get(registry).orElse(def);
  }

  /* ----------------------------- SUB CLASSES ------------------------------ */

  record KeyRef<V>(String key) implements Ref<V> {

    @Override
    public V orElseThrow(Registry<V> registry) {
      return registry.orThrow(key);
    }

    @Override
    public Optional<Holder<V>> getHolder(Registry<V> registry) {
      return registry.getHolder(key);
    }
  }

  record IdRef<V>(int id) implements Ref<V> {

    @Override
    public V orElseThrow(Registry<V> registry) {
      return registry.orThrow(id);
    }

    @Override
    public Optional<Holder<V>> getHolder(Registry<V> registry) {
      return registry.getHolder(id);
    }
  }

  record EmptyRef() implements Ref<Object> {

    @Override
    public Optional<Object> get(Registry<Object> registry) {
      return Optional.empty();
    }

    @Override
    public Object orElse(Registry<Object> registry, Object def) {
      return def;
    }

    @Override
    public @Nullable Object orNull(Registry<Object> registry) {
      return null;
    }

    @Override
    public Optional<Holder<Object>> getHolder(Registry<Object> registry) {
      return Optional.empty();
    }

    @Override
    public Object orElseThrow(Registry<Object> registry) {
      throw new IllegalArgumentException("Empty value reference");
    }
  }
}