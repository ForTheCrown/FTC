package net.forthecrown.core.commands.brigadier.types.custom;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import java.util.function.Function;

public abstract class CrownArgType<T> {
    public static <S> StringReader inputToReader(CommandContext<S> c, String s){
        StringReader reader = new StringReader(c.getInput());
        reader.setCursor(c.getInput().indexOf(c.getArgument(s, String.class)));

        return reader;
    }

    protected final DynamicCommandExceptionType exception;

    protected CrownArgType(Function<Object, Message> function){
        this.exception = new DynamicCommandExceptionType(function);
    }

    protected abstract T parse(StringReader reader) throws CommandSyntaxException;
}
