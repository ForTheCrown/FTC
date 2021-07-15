package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.CoreCommands;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.CrownCore;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.useables.warps.Warp;
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
        Key key = CoreCommands.ftcKeyType().parse(reader);

        if(!CrownCore.getWarpRegistry().contains(key)) throw UNKNOWN_WARP.createWithContext(GrenadierUtils.correctReader(reader, cursor), key.value());

        return key;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return listSuggestions(context, builder, false);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder, boolean ignoreChecks){
        if(ignoreChecks) return FtcSuggestionProvider.suggestRegistry(builder, CrownCore.getWarpRegistry());

        try {
            return CrownCore.getWarpRegistry().getSuggestions((CommandContext<CommandSource>) context, builder);
        } catch (CommandSyntaxException e) {
            return Suggestions.empty();
        }
    }
}
