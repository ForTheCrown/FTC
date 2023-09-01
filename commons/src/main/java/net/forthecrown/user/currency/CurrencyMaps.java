package net.forthecrown.user.currency;

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.forthecrown.user.Users;

public final class CurrencyMaps {
  private CurrencyMaps() {}

  public static final EmptyMap EMPTY_MAP = new EmptyMap();

  public static <T> CurrencyMap<T> emptyMap() {
    return EMPTY_MAP;
  }

  public static <T> CurrencyMap<T> unmodifiable(CurrencyMap<T> map) {
    if (map instanceof UnmodifiableMap<?> unmod) {
      return (CurrencyMap<T>) unmod;
    }
    if (map.isEmpty()) {
      return emptyMap();
    }
    return new UnmodifiableMap<>(map);
  }

  public static <T> CurrencyMap<T> newMap() {
    return new MapImpl<>(Users.getService().getCurrencies());
  }

  public static class EmptyMap<T>
      extends Object2ObjectMaps.EmptyMap<Currency, T>
      implements CurrencyMap<T>
  {

    @Override
    public T putCurrency(String key, T value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ObjectSet<String> idSet() {
      return ObjectSets.emptySet();
    }

    @Override
    public ObjectSet<Entry<String, T>> idEntrySet() {
      return ObjectSets.emptySet();
    }
  }

  public static class UnmodifiableMap<T>
      extends Object2ObjectMaps.UnmodifiableMap<Currency, T>
      implements CurrencyMap<T>
  {
    protected UnmodifiableMap(CurrencyMap<T> m) {
      super(m);
    }

    @Override
    public T putCurrency(String currency, T t) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ObjectSet<String> idSet() {
      return ObjectSets.unmodifiable(((CurrencyMap<?>) this.map).idSet());
    }

    @Override
    public ObjectSet<Entry<String, T>> idEntrySet() {
      return ObjectSets.unmodifiable(((CurrencyMap<T>) this.map).idEntrySet());
    }
  }
}
