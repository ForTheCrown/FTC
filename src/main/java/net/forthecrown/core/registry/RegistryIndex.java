package net.forthecrown.core.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import org.apache.logging.log4j.Logger;

@Getter
@RequiredArgsConstructor
public class RegistryIndex<V, I> {
  private static final Logger LOGGER = FTC.getLogger();

  private final Map<I, Holder<V>> index = new Object2ObjectOpenHashMap<>();
  private final IndexGetter<I, V> getter;

  void onRegister(Holder<V> holder) {
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