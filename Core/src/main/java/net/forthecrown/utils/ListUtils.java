package net.forthecrown.utils;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

public final class ListUtils {
    private ListUtils() {}

    public static <F, T> Set<T> convertToSet(@NotNull Collection<F> from, @NotNull Function<F, T> converter){
        return new HashSet<>(convert(from, converter));
    }

    public static <F, T> List<T> convert(@NotNull Collection<F> from, @NotNull Function<F, T> converter){
        Validate.notNull(from, "collection was null");
        Validate.notNull(converter, "Converter was null");

        List<T> convert = new ArrayList<>();

        for (F o: from){
            convert.add(converter.apply(o));
        }
        return convert;
    }

    public static <F, T> List<T> arrayToList(@NotNull F[] from, @NotNull Function<F, T> converter){
        return convert(Arrays.asList(from), converter);
    }

    public static <F, T> T[] convertArray(F[] from, IntFunction<T[]> arrayCreator, Function<F, T> function){
        T[] result = arrayCreator.apply(from.length);

        for (int i = 0; i < from.length; i++){
            if(from[i] == null) continue;
            result[i] = function.apply(from[i]);
        }
        return result;
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
        StringBuilder builder = new StringBuilder(FtcUtils.isNullOrBlank(prefix) ? "" : prefix);
        String acDelimiter = delimiter == null ? ", " : delimiter;

        int iteration = 0;
        for (I i: list){
            iteration++;

            builder.append(joiner.apply(i));
            if(iteration < list.size()) builder.append(acDelimiter);
        }

        if(!FtcUtils.isNullOrBlank(suffix)) builder.append(suffix);
        return builder.toString();
    }
}
