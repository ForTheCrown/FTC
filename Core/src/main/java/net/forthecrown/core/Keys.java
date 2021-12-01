package net.forthecrown.core;

import net.kyori.adventure.key.Key;

public interface Keys {
    static Key key(String namespace, String value) {
        return Key.key(namespace, value);
    }

    static Key ftccore(String value){
        return key(Crown.inst().namespace(), value);
    }
}
