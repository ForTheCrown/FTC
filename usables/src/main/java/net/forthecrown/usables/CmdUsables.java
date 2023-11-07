package net.forthecrown.usables;

import com.google.common.collect.Iterators;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.Permissions;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.usables.objects.CommandUsable;
import net.forthecrown.utils.io.SerializationHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CmdUsables<T extends CommandUsable> implements Iterable<T> {

  @Getter
  private final Factory<T> factory;

  private final Map<String, T> entries = new HashMap<>();

  @Getter
  private final Path file;

  public CmdUsables(Path file, Factory<T> factory) {
    this.factory = factory;
    this.file = file;
  }

  public void save() {
    SerializationHelper.writeTagFile(file, this::save);
  }

  public void load() {
    SerializationHelper.readTagFile(file, this::load);
  }

  public void save(CompoundTag tag) {
    for (var e : entries.entrySet()) {
      CompoundTag entryTag = BinaryTags.compoundTag();
      e.getValue().save(entryTag);
      tag.put(e.getKey(), entryTag);
    }
  }

  public void load(CompoundTag tag) {
    clear();

    for (var e : tag.entrySet()) {
      var name = e.getKey();
      var entryTag = (CompoundTag) e.getValue();

      var t = factory.create(name);
      t.load(entryTag);
      add(t);
    }
  }

  public Collection<T> getUsable(Player player) {
    List<T> result = new ArrayList<>();

    for (var e : entries.values()) {
      Interaction interaction = Interaction.create(player, e);

      if (!player.hasPermission(Permissions.ADMIN) && !e.test(interaction)) {
        continue;
      }

      result.add(e);
    }

    return result;
  }

  public void clear() {
    entries.clear();
  }

  public int size() {
    return entries.size();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public T get(String name) {
    return entries.get(name);
  }

  public void add(T t) {
    entries.put(t.getName(), t);
  }

  public boolean contains(String name) {
    return entries.containsKey(name);
  }

  public void remove(T t) {
    entries.remove(t.getName());
  }

  public Set<String> keySet() {
    return Collections.unmodifiableSet(entries.keySet());
  }

  public Collection<T> values() {
    return Collections.unmodifiableCollection(entries.values());
  }

  @NotNull
  @Override
  public Iterator<T> iterator() {
    return Iterators.unmodifiableIterator(entries.values().iterator());
  }

  public interface Factory<T extends CommandUsable> {
    T create(String name);
  }
}
