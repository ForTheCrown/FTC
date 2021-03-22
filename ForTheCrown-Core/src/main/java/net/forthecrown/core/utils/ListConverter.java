package net.forthecrown.core.utils;

import java.util.*;
import java.util.function.Function;

public final class ListConverter {
    private ListConverter() {}

    public static <F, T> Collection<T> convert(Collection<F> from, Function<F, T> converter){
        Collection<T> convert = new ArrayList<>();

        for (F o: from){
            convert.add(converter.apply(o));
        }
        return convert;
    }

    public static <F, T> Set<T> toSet(Collection<F> from, Function<F, T> converter){
        Set<T> convert = new HashSet<>();

        for (F o: from){
            convert.add(converter.apply(o));
        }
        return convert;
    }

    public static <F, T> List<T> toList(Collection<F> from, Function<F, T> converter){
        List<T> convert = new ArrayList<>();

        for (F o: from){
            convert.add(converter.apply(o));
        }
        return convert;
    }
}
