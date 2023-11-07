package net.forthecrown.core.placeholder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.forthecrown.text.Text;
import net.forthecrown.text.placeholder.PlaceholderContext;
import net.forthecrown.text.placeholder.PlaceholderList;
import net.forthecrown.text.placeholder.TextPlaceholder;

public class PlaceholderListImpl implements PlaceholderList {

  private final Map<String, TextPlaceholder> map = new HashMap<>();

  @Override
  public PlaceholderList add(String name, TextPlaceholder placeholder) {
    Objects.requireNonNull(name, "Null name");
    Objects.requireNonNull(placeholder, "Null placeholder");

    map.put(name, placeholder);
    return this;
  }

  @Override
  public PlaceholderList add(String name, Object value) {
    return add(name, (match, render) -> Text.valueOf(value, render.viewer()));
  }

  @Override
  public PlaceholderList add(String name, Supplier<?> supplier) {
    return add(name, (match, render) -> Text.valueOf(supplier.get(), render.viewer()));
  }

  @Override
  public PlaceholderList remove(String name) {
    map.remove(name);
    return this;
  }

  @Override
  public PlaceholderList clear() {
    map.clear();
    return this;
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Map<String, TextPlaceholder> getPlaceholders() {
    return Collections.unmodifiableMap(map);
  }

  @Override
  public TextPlaceholder getPlaceholder(String name, PlaceholderContext ctx) {
    return map.get(name);
  }
}
