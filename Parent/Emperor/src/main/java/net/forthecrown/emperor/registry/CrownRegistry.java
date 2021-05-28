package net.forthecrown.emperor.registry;

import net.forthecrown.emperor.utils.ListUtils;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.Set;

public interface CrownRegistry<T, R> {
    T get(Key key);

    T register(Key key, R raw);

    void remove(Key key);

    Set<Key> getKeys();

    default Set<String> getStringKeys(){
        return ListUtils.convertToSet(getKeys(), Key::value);
    }

    boolean contains(Key key);

    boolean contains(T value);

    Collection<T> getEntries();
}
