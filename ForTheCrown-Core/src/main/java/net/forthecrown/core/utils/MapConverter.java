package net.forthecrown.core.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class MapConverter {

    private MapConverter() {}

    public static <T, K, V> Map<K, T> convertValues(Map<K, V> map, Function<V, T> converter){
        Map<K, T> converted = new HashMap<>();
        for (K k: map.keySet()){
            converted.put(k, converter.apply(map.get(k)));
        }
        return converted;
    }

    public static <T, K, V> Map<T, V> convertKeys(Map<K, V> map, Function<K, T> converter){
        Map<T, V> tempMap = new HashMap<>();
        for (K k: map.keySet()){
            tempMap.put(converter.apply(k), map.get(k));
        }
        return tempMap;
    }

    public static <F, T, K, V> Map<F, T> convert(Map<K, V> map, Function<K, F> keyConverter, Function<V, T> valueConverter){
        Map<F, T> tempMap = new HashMap<>();
        for (K k: map.keySet()){
            tempMap.put(keyConverter.apply(k), valueConverter.apply(map.get(k)));
        }
        return tempMap;
    }
}
