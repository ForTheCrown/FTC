package net.forthecrown.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.CrownCore;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public final class SuggestionUtils {
    private SuggestionUtils() {}

    public static CompletableFuture<Suggestions> suggestKeys(SuggestionsBuilder builder, Iterable<Key> keys){
        return CompletionProvider.suggestKeys(builder, keys);
    }

    public static CompletableFuture<Suggestions> suggestKeysNoNamespace(SuggestionsBuilder builder, Iterable<Key> keys){
        return CompletionProvider.suggestMatching(builder, ListUtils.fromIterable(keys, Key::value));
    }

    public static CompletableFuture<Suggestions> suggestPlayernamesAndEmotes(CommandContext<CommandSource> c, SuggestionsBuilder builder, boolean ignorePerms){
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ')+1);

        CompletionProvider.suggestPlayerNames(builder);
        if(c.getSource().hasPermission(Permissions.DONATOR_3) || ignorePerms){
            return CrownCore.getEmotes().getSuggestions(c, builder, ignorePerms);
        }

        return builder.buildFuture();
    }
}