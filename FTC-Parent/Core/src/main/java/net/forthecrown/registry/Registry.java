package net.forthecrown.registry;

import net.kyori.adventure.key.Keyed;

public interface Registry<T> extends CrownRegistry<T, T>, Iterable<T>, Keyed {
    int size();

    void clear();

    boolean isEmpty();
}
