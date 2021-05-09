package net.forthecrown.core.commands.brigadier.types;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.types.signs.SignManager;
import net.forthecrown.core.types.signs.SignPrecondition;
import net.forthecrown.grenadier.CommandSource;

import java.util.concurrent.CompletableFuture;

public class SignPreconditionType implements ArgumentType<SignPrecondition> {
    private static final SignPreconditionType INSTANCE = new SignPreconditionType();
    private SignPreconditionType() {}

    public static final DynamicCommandExceptionType UNKNOWN_PRECONDITION = new DynamicCommandExceptionType(o -> () -> "Unknown precondition: " + o);

    public static SignPreconditionType precondition(){
        return INSTANCE;
    }

    @Override
    public SignPrecondition parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        try {
            return SignManager.getPrecondition(name);
        } catch (NullPointerException e){
            reader.setCursor(cursor);
            throw UNKNOWN_PRECONDITION.createWithContext(reader, name);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(builder, SignManager.getPreconditions());
    }
}
