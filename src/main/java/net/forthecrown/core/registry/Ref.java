package net.forthecrown.core.registry;

import java.util.Optional;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

 /**
 * A reference to an entry within a registry
 *
 * <p>
 * Use example:
 * <pre><code>
 * Registry&lt;UserRank&gt; registry = // ...
 *
 * KeyRef&lt;UserRank&gt; ref = Ref.key(registry, "default");
 *
 * Optional&lt;UserRank&gt; value = ref.get();
 * String key = ref.getKey();
 * </code></pre>
 *
 * @param <V> Registry's type
 */
public interface Ref<V> {

  /**
   * Gets the reference's registry
   * @return Registry
   */
  Registry<V> getRegistry();

  /**
   * Gets the referenced value
   *
   * @return Referenced value, empty optional, if the value pointed to by this
   *         reference, was not found
   */
  Optional<V> get();

  default @Nullable V orNull() {
    return get().orElse(null);
  }

  default V orElseThrow() {
    return get().orElseThrow();
  }

  default V orElse(V def) {
    return get().orElse(def);
  }

  static <V> IdRef<V> id(Registry<V> registry, int id) {
    return new IdRef<>(registry, id);
  }

  static <V> KeyRef<V> key(Registry<V> registry, String name) {
    return new KeyRef<>(registry, name);
  }

  static <V> Ref<V> empty(Registry<V> registry) {
    return new Ref<>() {
      @Override
      public Registry<V> getRegistry() {
        return registry;
      }

      @Override
      public Optional<V> get() {
        return Optional.empty();
      }
    };
  }

  abstract class AbstractRef<V> implements Ref<V> {
    private final Registry<V> registry;

    public AbstractRef(Registry<V> registry) {
      this.registry = registry;
    }

    @Override
    public Registry<V> getRegistry() {
      return registry;
    }
  }

  @Getter
  class KeyRef<V> extends AbstractRef<V> {

    private final String key;

    public KeyRef(Registry<V> registry, String key) {
      super(registry);
      this.key = key;
    }

    @Override
    public Optional<V> get() {
      return getRegistry().get(key);
    }
  }

  @Getter
  class IdRef<V> extends AbstractRef<V> {

    private final int id;

    public IdRef(Registry<V> registry, int id) {
      super(registry);
      this.id = id;
    }

    @Override
    public Optional<V> get() {
      return Optional.empty();
    }
  }
}