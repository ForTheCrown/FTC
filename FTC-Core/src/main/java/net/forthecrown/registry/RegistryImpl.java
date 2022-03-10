package net.forthecrown.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

// Just kidding, registries are nothing more than a wrapper for a
// hash map, get pranked
public class RegistryImpl<V> implements Registry<V> {
    private final Object2ObjectMap<Key, V> entries = new Object2ObjectOpenHashMap<>();
    private final NamespacedKey key;

    public RegistryImpl(NamespacedKey key) {
        this.key = key;
    }

    @NotNull
    @Override
    public Iterator<V> iterator() {
        return entries.values().iterator();
    }

    @Override
    public V get(Key key) {
        return entries.get(FtcUtils.ensureBukkit(key));
    }

    @Override
    public V register(Key key, V value) {
        entries.put(FtcUtils.ensureBukkit(key), value);
        return value;
    }

    @Override
    public V remove(Key key) {
        return entries.remove(FtcUtils.ensureBukkit(key));
    }

    @Override
    public Set<Key> keySet() {
        return entries.keySet();
    }

    @Override
    public boolean contains(Key key) {
        return entries.containsKey(FtcUtils.ensureBukkit(key));
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
    public Key getKey(V val) {
        for (Object2ObjectMap.Entry<Key, V> e: entries.object2ObjectEntrySet()) {
             if(e.getValue().equals(val)) return e.getKey();
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RegistryImpl<?> registry = (RegistryImpl<?>) o;

        return new EqualsBuilder()
                .append(entries, registry.entries)
                .append(key, registry.key)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @Override
    public String toString() {
        return entries.toString();
    }

    @Override
    public @NotNull NamespacedKey key() {
        return key;
    }
}
