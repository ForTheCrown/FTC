package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;

import java.util.concurrent.CompletableFuture;

public class JailType implements ArgumentType<Key> {
    public static final JailType JAIL = new JailType();
    private JailType() {}

    public static final DynamicCommandExceptionType UNKNOWN_JAIL = new DynamicCommandExceptionType(o -> () -> "Unknown jail: " + o);

    public static JailType jail(){
        return JAIL;
    }

    public static Location getJailLocation(CommandContext<CommandSource> c, String argument){
        return CrownCore.getJailManager().get(c.getArgument(argument, Key.class));
    }

    @Override
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = FtcUtils.parseKey(reader);

        if(!CrownCore.getJailManager().contains(key)){
            throw UNKNOWN_JAIL.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), key.asString());
        }

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return FtcSuggestionProvider.suggestKeysNoNamespace(builder, CrownCore.getJailManager().keySet());
    }
}
