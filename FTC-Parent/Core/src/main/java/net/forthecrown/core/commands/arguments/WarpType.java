package net.forthecrown.core.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.useables.warps.Warp;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public class WarpType implements ArgumentType<Key> {
    public static final WarpType WARP = new WarpType();
    private WarpType() {}

    public static final DynamicCommandExceptionType UNKNOWN_WARP = new DynamicCommandExceptionType(o -> () -> "Unknown warp: " + o);

    public static WarpType warp(){
        return WARP;
    }

    public static Warp getWarp(CommandContext<CommandSource> c, String argument){
        return CrownCore.getWarpRegistry().get(c.getArgument(argument, Key.class));
    }

    @Override
    public Key  parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        Key key = CrownUtils.parseKey(reader);

        if(!CrownCore.getWarpRegistry().contains(key)) throw UNKNOWN_WARP.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), key.value());

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return listSuggestions(context, builder, false);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder, boolean ignoreChecks){
        if(ignoreChecks) return CrownUtils.suggestKeys(builder, CrownCore.getWarpRegistry().getKeys());

        try {
            return CrownCore.getWarpRegistry().getSuggestions((CommandContext<CommandSource>) context, builder);
        } catch (CommandSyntaxException e) {
            return Suggestions.empty();
        }
    }
}
