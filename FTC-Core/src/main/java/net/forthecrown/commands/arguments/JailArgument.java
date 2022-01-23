package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Crown;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.Keys;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;

import java.util.concurrent.CompletableFuture;

public class JailArgument implements ArgumentType<Key> {
    public static final JailArgument JAIL = new JailArgument();
    private JailArgument() {}

    public static final DynamicCommandExceptionType UNKNOWN_JAIL = new DynamicCommandExceptionType(o -> () -> "Unknown jail: " + o);

    public static JailArgument jail(){
        return JAIL;
    }

    public static Location getJailLocation(CommandContext<CommandSource> c, String argument){
        return Crown.getJailManager().get(c.getArgument(argument, Key.class));
    }

    @Override
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = Keys.parse(reader);

        if(!Crown.getJailManager().contains(key)){
            throw UNKNOWN_JAIL.createWithContext(GrenadierUtils.correctReader(reader, cursor), key.asString());
        }

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return FtcSuggestionProvider.suggestKeysNoNamespace(builder, Crown.getJailManager().keySet());
    }
}
