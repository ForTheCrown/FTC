package net.forthecrown.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class BaseRegistry<V> implements Registry<V> {
    private final Object2ObjectMap<Key, V> entries = new Object2ObjectOpenHashMap<>();
    private final Key key;

    public BaseRegistry(Key key) {
        this.key = FtcUtils.checkNotBukkit(key);
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return entries.values().iterator();
    }

    @Override
    public V get(Key key) {
        key = FtcUtils.checkNotBukkit(key);
        return entries.get(key);
    }

    @Override
    public V register(Key key, V value) {
        key = FtcUtils.checkNotBukkit(key);
        entries.put(key, value);
        return value;
    }

    @Override
    public void remove(Key key) {
        key = FtcUtils.checkNotBukkit(key);
        entries.remove(key);
    }

    @Override
    public Set<Key> keySet() {
        return entries.keySet();
    }

    @Override
    public boolean contains(Key key) {
        key = FtcUtils.checkNotBukkit(key);
        return entries.containsKey(key);
    }

    @Override
    public boolean contains(V value) {
        return entries.containsValue(value);
    }

    @Override
    public Collection<V> values() {
        return entries.values();
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BaseRegistry<?> registry = (BaseRegistry<?>) o;

        return new EqualsBuilder()
                .append(values(), registry.values())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(values())
                .toHashCode();
    }

    @Override
    public String toString() {
        return entries.toString();
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
