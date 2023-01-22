package net.forthecrown.core.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.logging.Loggers;
import org.apache.logging.log4j.Logger;

@Getter
@RequiredArgsConstructor
public class RegistryIndex<V, I> implements RegistryListener<V> {
  private static final Logger LOGGER = Loggers.getLogger();

  private final Map<I, Holder<V>> index = new Object2ObjectOpenHashMap<>();
  private final IndexGetter<I, V> getter;

  public void onRegister(Holder<V> holder) {
    I val = getter.get(holder);

    if (val == null) {
      return;
    }

    var existing = index.put(val, holder);

    if (existing != null) {
      LOGGER.warn("found duplicate index key for entry: '{}'",
          holder.getKey()
      );
    }
  }

  @Override
  public void onUnregister(Holder<V> value) {
    var val = getter.get(value);

    if (val == null) {
      return;
    }

    index.remove(val);
  }

  public Optional<Holder<V>> lookup(I val) {
    return Optional.ofNullable(index.get(val));
  }

  public Optional<V> lookupValue(I val) {
    return lookup(val).map(Holder::getValue);
  }

  public Optional<String> lookupKey(I val) {
    return lookup(val).map(Holder::getKey);
  }

  public Optional<Integer> lookupId(I val) {
    return lookup(val).map(Holder::getId);
  }

  public interface IndexGetter<I, V> {
    I get(Holder<V> holder);
  }
}