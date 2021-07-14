package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.registry.Registries;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.useables.actions.UsageAction;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

/**
 * Parses an UsageAction type, currently only used in the {@link net.forthecrown.commands.CommandInteractable}
 */
public class ActionArgType implements ArgumentType<Key> {
    private static final ActionArgType INSTANCE = new ActionArgType();
    private ActionArgType() {}

    //Exception to throw for an unknown action
    public static DynamicCommandExceptionType UNKNOWN_ACTION = new DynamicCommandExceptionType(o -> () -> "Unknown action: " + o);

    //Returns the single static instance of this class
    public static ActionArgType action(){
        return INSTANCE;
    }

    //Just a convenience method for getting the type from the returned key
    public static UsageAction<?> getAction(CommandContext<CommandSource> c, String argument){
        return Registries.USAGE_ACTIONS.get(c.getArgument(argument, Key.class));
    }

    @Override
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = KeyType.ftc().parse(reader); //parse the key

        try {
            Registries.USAGE_ACTIONS.get(key).key(); //Couldn't be arsed doing a if blabla == null thing here, so this shit it is lol
        } catch (NullPointerException e){
            throw UNKNOWN_ACTION.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), key.asString());
        } //GrenadierUtils.correctCursorReader returns a reader that's been moved back to the correct position to correctly highlight things

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return FtcSuggestionProvider.suggestRegistry(builder, Registries.USAGE_ACTIONS);
    }
}
