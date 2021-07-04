package net.forthecrown.registry;

import net.forthecrown.utils.CrownUtils;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BaseRegistry<V> implements Registry<V> {
    private final Map<Key, V> entries = new HashMap<>();

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return entries.values().iterator();
    }

    @Override
    public V get(Key key) {
        key = CrownUtils.checkNotBukkit(key);
        return entries.get(key);
    }

    @Override
    public V register(Key key, V raw) {
        key = CrownUtils.checkNotBukkit(key);
        entries.put(key, raw);
        return raw;
    }

    @Override
    public void remove(Key key) {
        key = CrownUtils.checkNotBukkit(key);
        entries.remove(key);
    }

    @Override
    public Set<Key> getKeys() {
        return entries.keySet();
    }

    @Override
    public boolean contains(Key key) {
        key = CrownUtils.checkNotBukkit(key);
        return entries.containsKey(key);
    }

    @Override
    public boolean contains(V value) {
        return entries.containsValue(value);
    }

    @Override
    public Collection<V> getEntries() {
        return entries.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BaseRegistry<?> registry = (BaseRegistry<?>) o;

        return new EqualsBuilder()
                .append(getEntries(), registry.getEntries())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getEntries())
                .toHashCode();
    }

    @Override
    public String toString() {
        return entries.toString();
    }
}
