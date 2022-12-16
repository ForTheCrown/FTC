package net.forthecrown.useables.command;

import com.google.common.collect.Iterators;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import net.forthecrown.utils.io.SerializableObject;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public class CmdUsables<T extends CommandUsable> extends SerializableObject.NbtDat implements Iterable<T> {
    /** Entry factory to deserialize entries */
    @Getter
    private final EntryFactory<T> factory;

    private final Map<String, T> entries = new HashMap<>();

    public CmdUsables(Path file, EntryFactory<T> factory) {
        super(file);
        this.factory = factory;
    }

    @Override
    public void save(CompoundTag tag) {
        for (var e: entries.entrySet()) {
            tag.put(e.getKey(), e.getValue().save());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        clear();

        for (var e: tag.tags.entrySet()) {
            var name = e.getKey();
            var entryTag = (CompoundTag) e.getValue();

            try {
                var t = factory.create(name, entryTag);
                add(t);
            } catch (CommandSyntaxException exc) {
                exc.printStackTrace();
            }
        }
    }

    public Collection<T> getUsable(Player player) {
        List<T> result = new ArrayList<>();

        for (var e: entries.values()) {
            if (!e.test(player)) {
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

    public interface EntryFactory<E> {
        E create(String name, CompoundTag tag) throws CommandSyntaxException;
    }
}