package net.forthecrown.core.commands.brigadier.types.custom;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.utils.ListUtils;
import net.minecraft.server.v1_16_R3.CommandListenerWrapper;

public class EnumArgType<T extends Enum<T>> extends CrownArgType<T> {

    private final Class<T> clazz;
    protected EnumArgType(Class<T> clazz) {
        super(obj -> new LiteralMessage("Unkown " + clazz.getSimpleName() + ": " + obj.toString()));
        this.clazz = clazz;
    }

    @Override
    protected T parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        T result;
        try {
            result = T.valueOf(clazz, name.toUpperCase());
        } catch (IllegalArgumentException e){
            reader.setCursor(cursor);
            throw exception.createWithContext(reader, name);
        }

        return result;
    }

    public <S> T get(CommandContext<S> context, String argument) throws CommandSyntaxException {
        return parse(inputToReader(context, argument));
    }

    public SuggestionProvider<CommandListenerWrapper> suggests(boolean lowerCase){
        return CrownCommandBuilder.suggest(ListUtils.arrayToCollection(clazz.getEnumConstants(), (lowerCase ? t -> t.toString().toLowerCase() : T::toString)));
    }

    public static StringArgumentType enumType(){
        return StringArgumentType.word();
    }

    public static <E extends Enum<E>, S> E getEnum(CommandContext<S> c, String argument, Class<E> clazz) throws CommandSyntaxException {
        return new EnumArgType<E>(clazz).get(c, argument);
    }
}
