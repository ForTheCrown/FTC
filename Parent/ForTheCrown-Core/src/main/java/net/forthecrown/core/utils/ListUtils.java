package net.forthecrown.core.utils;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public final class ListUtils {
    private ListUtils() {}

    public static <F, T> Collection<T> convert(@NotNull Collection<F> from, @NotNull Function<F, T> converter){
        Validate.notNull(from, "collection was null");
        Validate.notNull(converter, "Converter was null");

        Collection<T> convert = new ArrayList<>();

        for (F o: from){
            convert.add(converter.apply(o));
        }
        return convert;
    }

    public static <F, T> Set<T> convertToSet(@NotNull Collection<F> from, @NotNull Function<F, T> converter){
        return new HashSet<>(convert(from, converter));
    }

    public static <F, T> List<T> convertToList(@NotNull Collection<F> from, @NotNull Function<F, T> converter){
        return new ArrayList<>(convert(from, converter));
    }

    public static <F, T> Collection<T> arrayToCollection(@NotNull F[] from, @NotNull Function<F, T> converter){
        return convert(Arrays.asList(from), converter);
    }

    public static boolean isNullOrEmpty(@Nullable Collection<?> collection){
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean isNullOrEmpty(T[] array){
        if(array == null || array.length == 0) return true;
        for (T t: array){
            if(t != null) return false;
        }
        return true;
    }
}
