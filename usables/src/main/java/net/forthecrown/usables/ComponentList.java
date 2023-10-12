package net.forthecrown.usables;


import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.textOfChildren;

import com.google.common.base.Strings;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import net.forthecrown.Loggers;
import net.forthecrown.registry.Registry;
import net.forthecrown.text.TextWriter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class ComponentList<T extends UsableComponent> implements Iterable<T> {

  public static final Logger LOGGER = Loggers.getLogger();

  private final Registry<ObjectType<T>> registry;

  private T[] contents;
  private int size;

  @SuppressWarnings({"rawtypes", "unchecked"})
  private ComponentList(Registry<ObjectType<? extends T>> registry, Class<T> arrayType) {
    Objects.requireNonNull(registry, "Null registry");

    this.registry = (Registry) registry;

    this.size = 0;
    this.contents = (T[]) Array.newInstance(arrayType, 0);
  }

  public static ComponentList<Action> newActionList() {
    return new ComponentList<>(UsablesPlugin.get().getActions(), Action.class);
  }

  public static ComponentList<Condition> newConditionList() {
    return new ComponentList<>(UsablesPlugin.get().getConditions(), Condition.class);
  }

  private void grow() {
    grow(size + 1);
  }

  private void grow(int newSize) {
    if (contents.length >= newSize) {
      return;
    }

    contents = ObjectArrays.grow(contents, newSize);
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public int size() {
    return size;
  }

  public void clear() {
    Arrays.fill(contents, null);
    size = 0;
  }

  public void addFirst(T value) {
    add(value, 0);
  }

  public void addLast(T value) {
    add(value, size);
  }

  public void add(T value, int index) {
    Objects.requireNonNull(value, "Null value");
    Objects.checkIndex(index, size + 1);

    grow();

    if (index != size) {
      System.arraycopy(contents, index, contents, index + 1, size - index);
    }

    contents[index] = value;
    size++;
  }

  public void set(int index, T value) {
    Objects.requireNonNull(value, "Null value");
    Objects.checkIndex(index, size);
    contents[index] = value;
  }

  public T remove(int index) {
    Objects.checkIndex(index, size);

    T removed = contents[index];
    size--;

    if (index != size) {
      System.arraycopy(contents, index + 1, contents, index, size - index);
    }

    contents[size] = null;
    return removed;
  }

  public void removeBetween(int fromIndex, int toIndex) {
    Objects.checkFromToIndex(fromIndex, toIndex, size);
    int removeObjects = toIndex - fromIndex;
    for (int i = 0; i < removeObjects; i++) {
      remove(fromIndex);
    }
  }

  public void removeWithType(ObjectType<? extends T> type) {
    var it = iterator();

    while (it.hasNext()) {
      var n = it.next();

      if (Objects.equals(type, n.getType())) {
        it.remove();
      }
    }
  }

  @NotNull
  @Override
  public Iterator<T> iterator() {
    return new Iter();
  }

  @SuppressWarnings("unchecked")
  public <S> DataResult<S> save(DynamicOps<S> ops) {
    ListBuilder<S> builder = ops.listBuilder();

    for (T t : this) {
      ObjectType<T> type = (ObjectType<T>) t.getType();

      // Transient type
      if (type == null) {
        continue;
      }

      registry.getKey(type).ifPresentOrElse(key -> {
        var mapBuilder = ops.mapBuilder();
        mapBuilder.add("type", ops.createString(key));

        type.save(t, ops)
            .mapError(s -> "Failed to save '" + key + "': " + s)
            .resultOrPartial(LOGGER::error)
            .filter(s -> !Objects.equals(s, ops.empty()))
            .ifPresent(s -> mapBuilder.add("value", s));

        builder.add(mapBuilder.build(ops.empty()));
      }, () -> {
        LOGGER.error("UsageType {} is not registered", type);
      });
    }

    return builder.build(ops.emptyList());
  }

  public <S> void load(Dynamic<S> dynamic) {
    clear();

    List<Dynamic<S>> dynamicList = dynamic.asList(Function.identity());

    if (dynamicList.isEmpty()) {
      return;
    }

    for (Dynamic<S> pair : dynamicList) {
      Optional<String> keyOptional = pair.get("type").asString()
          .mapError(s -> "Error getting 'type': " + s)
          .resultOrPartial(LOGGER::error);

      if (keyOptional.isEmpty()) {
        continue;
      }

      String key = keyOptional.get();

      registry.get(key).ifPresentOrElse(type -> {
        Dynamic<S> valueElement = pair.get("value")
            .result()
            .orElse(null);

        if (valueElement == null) {
          try {
            var value = type.createEmpty();
            addLast(value);
          } catch (UnsupportedOperationException exc) {
            LOGGER.error("Type '{}' didn't support createEmpty() but has no data to load",
                key, exc
            );
          }

          return;
        }

        type.load(valueElement)
            .mapError(s -> "Error loading " + key + ": " + s)
            .resultOrPartial(LOGGER::error)
            .ifPresent(this::addLast);

      }, () -> {
        LOGGER.error("Couldn't find component with key '{}'", key);
      });
    }
  }

  public void write(TextWriter writer, String commandPrefix) {
    if (isEmpty()) {
      writer.write("[]");
      return;
    }

    if (!Strings.isNullOrEmpty(commandPrefix)) {
      writer.write(
          text("[clear] ", NamedTextColor.AQUA)
              .hoverEvent(text("Clears the list"))
              .clickEvent(ClickEvent.runCommand(commandPrefix + " clear"))
      );

      writer.write(
          text("[add] ", NamedTextColor.GREEN)
              .hoverEvent(text("Suggests a command to add an element"))
              .clickEvent(ClickEvent.suggestCommand(commandPrefix + " add "))
      );
    }

    writer.write("[");

    var prefixed = writer.withIndent(2);

    for (int i = 0; i < size; i++) {
      int viewIndex = i + 1;

      if (Strings.isNullOrEmpty(commandPrefix)) {
        if (!prefixed.isLineEmpty()) {
          prefixed.newLine();
        }
      } else {
        prefixed.line(
            text("(❌)", NamedTextColor.RED)
                .hoverEvent(text("Removes this element"))
                .clickEvent(ClickEvent.runCommand(commandPrefix + " remove " + viewIndex))
        );

        prefixed.space();
      }

      prefixed.write(viewIndex + ") ", NamedTextColor.GRAY);

      Component display = displayEntry(i);
      prefixed.write(display);
    }

    writer.line("]");
  }

  public Component displayEntry(int index) {
    T component = contents[index];
    ObjectType<T> type = (ObjectType<T>) component.getType();

    Component prefix;

    if (type == null) {
      prefix = text("TRANSIENT", NamedTextColor.YELLOW);
    } else {
      prefix = registry.getKey(type)
          .map(s -> text(s, NamedTextColor.YELLOW))
          .orElseGet(() -> text("UNKNOWN", NamedTextColor.YELLOW));
    }

    Component displayInfo = component.displayInfo();

    if (displayInfo == null) {
      return prefix;
    } else {
      return textOfChildren(prefix, text(": "), displayInfo);
    }
  }

  private class Iter implements Iterator<T> {
    int i = 0;
    int current = -1;

    @Override
    public boolean hasNext() {
      return i < size;
    }

    @Override
    public void remove() {
      if (current == -1) {
        throw new NoSuchElementException("remove() already called or next() not called at all");
      }

      ComponentList.this.remove(i);
      i--;
      current = -1;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      current = i;
      return contents[i++];
    }
  }
}
