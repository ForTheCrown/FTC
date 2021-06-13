package net.forthecrown.core.utils;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class MapUtils {

    private MapUtils() {}

    public static <T, K, V> Map<K, T> convertValues(Map<K, V> map, Function<V, T> converter){
        Map<K, T> tempMap = new HashMap<>();
        for (Map.Entry<K, V> k: map.entrySet()){
            tempMap.put(k.getKey(), converter.apply(k.getValue()));
        }
        return tempMap;
    }

    public static <T, K, V> Map<T, V> convertKeys(Map<K, V> map, Function<K, T> converter){
        Map<T, V> tempMap = new HashMap<>();
        for (Map.Entry<K, V> k: map.entrySet()){
            tempMap.put(converter.apply(k.getKey()), k.getValue());
        }
        return tempMap;
    }

    public static <F, T, K, V> Map<F, T> convert(Map<K, V> map, Function<K, F> keyConverter, Function<V, T> valueConverter){
        Map<F, T> tempMap = new HashMap<>();
        for (Map.Entry<K, V> k: map.entrySet()){
            tempMap.put(keyConverter.apply(k.getKey()), valueConverter.apply(k.getValue()));
        }
        return tempMap;
    }

    public static boolean isNullOrEmpty(@Nullable Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
