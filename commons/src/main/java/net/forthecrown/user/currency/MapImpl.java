package net.forthecrown.user.currency;

import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import net.forthecrown.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class MapImpl<T> implements CurrencyMap<T> {

  private final Object2ObjectOpenHashMap<String, T> backing;
  private final Registry<Currency> registry;

  public MapImpl(Registry<Currency> registry) {
    this.registry = registry;
    this.backing = new Object2ObjectOpenHashMap<>();
  }

  @Override
  public void defaultReturnValue(T rv) {
    backing.defaultReturnValue(rv);
  }

  @Override
  public T defaultReturnValue() {
    return backing.defaultReturnValue();
  }

  @Override
  public ObjectSet<Entry<Currency, T>> object2ObjectEntrySet() {
    return new EntrySet<>(backing.object2ObjectEntrySet());
  }

  @Override
  public ObjectSet<Currency> keySet() {
    return new KeySet(backing.keySet());
  }

  @Override
  public ObjectCollection<T> values() {
    return backing.values();
  }

  @Override
  public ObjectSet<String> idSet() {
    return backing.keySet();
  }

  @Override
  public ObjectSet<Entry<String, T>> idEntrySet() {
    return backing.object2ObjectEntrySet();
  }

  @Override
  public T putCurrency(String currencyId, T value) {
    return backing.put(currencyId, value);
  }

  @Override
  public void putAll(@NotNull Map<? extends Currency, ? extends T> m) {
    Objects.requireNonNull(m);
    m.forEach(this::put);
  }

  @Override
  public int size() {
    return backing.size();
  }

  @Override
  public boolean isEmpty() {
    return backing.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    if (key instanceof String str) {
      return backing.containsKey(str);
    } else if (key instanceof Currency currency) {
      return registry.getKey(currency).map(backing::containsKey).orElse(false);
    }

    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    return backing.containsValue(value);
  }

  @Override
  public T get(Object key) {
    if (key instanceof String string) {
      return backing.get(string);
    } else if (key instanceof Currency currency) {
      return registry.getKey(currency).map(backing::get).orElse(null);
    }
    return null;
  }

  @Nullable
  @Override
  public T put(Currency key, T value) {
    return registry.getKey(key).map(string -> backing.put(string, value)).orElse(null);
  }

  @Override
  public T remove(Object key) {
    if (key instanceof String string) {
      return backing.remove(string);
    } else if (key instanceof Currency currency) {
      return registry.getKey(currency).map(backing::remove).orElse(null);
    }
    return null;
  }

  @Override
  public void clear() {
    backing.clear();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MapImpl<?> map)) {
      return false;
    }
    return backing.equals(map.backing);
  }

  @Override
  public int hashCode() {
    return backing.hashCode();
  }

  class EntrySet<T> extends AbstractObjectSet<Entry<Currency, T>> {

    ObjectSet<Entry<String, T>> backing;

    public EntrySet(ObjectSet<Entry<String, T>> backing) {
      this.backing = backing;
    }

    @Override
    public boolean remove(Object o) {
      return backing.remove(o);
    }

    @Override
    public ObjectIterator<Entry<Currency, T>> iterator() {
      return new EntryIter<>(backing.iterator());
    }

    @Override
    public int size() {
      return backing.size();
    }
  }

  class EntryIter<T>
      extends AbstractIterator<Entry<Currency, T>>
      implements ObjectIterator<Entry<Currency, T>>
  {

    private final ObjectIterator<Entry<String, T>> backing;
    private final WrappedEntry<T> entry = new WrappedEntry<>();

    public EntryIter(ObjectIterator<Entry<String, T>> backing) {
      this.backing = backing;
    }

    @Nullable
    @Override
    protected Entry<Currency, T> computeNext() {
      if (!backing.hasNext()) {
        return endOfData();
      }

      while (backing.hasNext()) {
        var n = backing.next();
        var opt = registry.get(n.getKey());

        if (opt.isEmpty()) {
          continue;
        }

        entry.entry = n;
        entry.currency = opt.get();

        return entry;
      }

      return endOfData();
    }
  }

  class WrappedEntry<T> implements Entry<Currency, T> {

    Entry<String, T> entry;
    Currency currency;

    boolean present() {
      return currency != null && entry != null;
    }

    @Override
    public Currency getKey() {
      return currency;
    }

    @Override
    public T getValue() {
      return entry.getValue();
    }

    @Override
    public T setValue(T value) {
      return entry.setValue(value);
    }
  }

  class KeySet extends AbstractObjectSet<Currency> {

    private final ObjectCollection<String> backing;

    public KeySet(ObjectCollection<String> backing) {
      this.backing = backing;
    }

    @Override
    public boolean remove(Object o) {
      return MapImpl.this.remove(o) != null;
    }

    @Override
    public ObjectIterator<Currency> iterator() {
      return new ObjectIterator<>() {
        Currency next;
        String nextStr;

        ObjectIterator<String> backingIt = backing.iterator();

        @Override
        public boolean hasNext() {
          if (!backingIt.hasNext()) {
            return false;
          }

          if (next != null) {
            return true;
          }

          while (backingIt.hasNext()) {
            var str = backingIt.next();
            var opt = registry.get(str);

            if (opt.isEmpty()) {
              continue;
            }

            nextStr = str;
            next = opt.get();

            return true;
          }

          return next != null;
        }

        @Override
        public Currency next() {
          if (!hasNext()) {
            throw new NoSuchElementException();
          }

          var cur = next;
          next = null;

          return cur;
        }

        @Override
        public void remove() {
          backingIt.remove();
        }
      };
    }

    @Override
    public int size() {
      return backing.size();
    }
  }
}
