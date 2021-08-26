package net.forthecrown.comvars;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

//Normal Function<I, R> wouldn't work cuz this needs to throw a CommandSyntaxException
@FunctionalInterface
public interface ParseFunction<T> {
    T parse(StringReader reader) throws CommandSyntaxException;
}
