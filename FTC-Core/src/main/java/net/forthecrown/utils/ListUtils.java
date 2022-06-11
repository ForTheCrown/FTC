package net.forthecrown.utils;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * Utilities relating to lists and collections, mostly involving either
 * converting lists from one type to another or joining lists together
 * into strings
 */
public final class ListUtils {
    private ListUtils() {}

    public static <F, T> List<T> convert(@NotNull Collection<F> from, @NotNull Function<F, T> converter){
        Validate.notNull(from, "collection was null");
        Validate.notNull(converter, "Converter was null");

        List<T> convert = new ArrayList<>();

        for (F o: from){
            convert.add(converter.apply(o));
        }
        return convert;
    }

    public static <F, T> List<T> fromIterable(Iterable<F> from, Function<F, T> converter){
        List<T> convert = new ArrayList<>();

        for (F f: from){
            convert.add(converter.apply(f));
        }
        return convert;
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

    public static <I> String join(@NotNull Collection<I> list, @Nullable String delimiter, @NotNull Function<I, String> stringMaker){
        return join(list, delimiter, null, null, stringMaker);
    }

    public static <I> String join(@NotNull Collection<I> list, @NotNull Function<I, String> stringFunction){
        return join(list, null, null, null, stringFunction);
    }

    public static <I> String join(@NotNull Collection<I> list, @Nullable String delimiter, @Nullable String prefix, @Nullable String suffix, @NotNull Function<I, String> joiner){
        StringJoiner stringJoiner = new StringJoiner(
                delimiter == null ? ", " : delimiter,
                prefix == null ? "" : prefix,
                suffix == null ? "" : suffix
        );

        for (I i: list) {
            stringJoiner.add(joiner.apply(i));
        }

        return stringJoiner.toString();
    }
}