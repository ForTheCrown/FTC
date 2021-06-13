package net.forthecrown.core.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.useables.kits.Kit;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public class KitType implements ArgumentType<Key> {
    public static final KitType KIT = new KitType();
    private KitType() {}

    public static final DynamicCommandExceptionType UNKNOWN_KIT = new DynamicCommandExceptionType(o -> () -> "Unknown kit: " + o);

    public static KitType kit(){
        return KIT;
    }

    public static Kit getKit(CommandContext<CommandSource> c, String argument){
        return CrownCore.getKitRegistry().get(c.getArgument(argument, Key.class));
    }

    @Override
    public Key parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = CrownUtils.parseKey(reader);

        if(!CrownCore.getKitRegistry().contains(key)) throw UNKNOWN_KIT.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), key);

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return listSuggestions(context, builder, false);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder, boolean ignoreChecks){
        if(ignoreChecks) return CrownUtils.suggestKeys(builder, CrownCore.getKitRegistry().getKeys());

        try {
            return CrownCore.getKitRegistry().getSuggestions((CommandContext<CommandSource>) context, builder);
        } catch (CommandSyntaxException ignored) {}
        return Suggestions.empty();
    }
}
