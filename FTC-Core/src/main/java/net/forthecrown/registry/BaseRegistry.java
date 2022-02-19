package net.forthecrown.registry;

import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;

import java.util.Collection;
import java.util.Set;

public interface BaseRegistry<T, R> {
    T get(Key key);

    T register(Key key, R value);

    T remove(Key key);

    Set<Key> keySet();

    default Set<String> getStringKeys(){
        return ListUtils.convertToSet(keySet(), Key::value);
    }

    boolean contains(Key key);

    boolean contains(T value);

    Collection<T> values();
}
