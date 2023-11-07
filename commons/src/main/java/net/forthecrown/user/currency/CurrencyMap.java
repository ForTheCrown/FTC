package net.forthecrown.user.currency;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public interface CurrencyMap<T> extends Object2ObjectMap<Currency, T> {

  ObjectSet<String> idSet();

  ObjectSet<Entry<String, T>> idEntrySet();

  T putCurrency(String currencyId, T value);

}
