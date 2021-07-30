package net.forthecrown.commands.manager;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.ForTheCrown;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.registry.CrownRegistry;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.UserManager;
import net.forthecrown.utils.ListUtils;
import net.kyori.adventure.key.Key;

import java.util.concurrent.CompletableFuture;

public interface FtcSuggestionProvider {
    static CompletableFuture<Suggestions> suggestRegistry(SuggestionsBuilder builder, CrownRegistry<?, ?> registry){
        return CompletionProvider.suggestKeys(builder, registry.keySet());
    }

    static CompletableFuture<Suggestions> suggestKeysNoNamespace(SuggestionsBuilder builder, Iterable<Key> keys){
        return CompletionProvider.suggestMatching(builder, ListUtils.fromIterable(keys, Key::value));
    }

    static CompletableFuture<Suggestions> suggestPlayerNames(CommandSource c, SuggestionsBuilder builder){
        String token = builder.getRemainingLowerCase();
        boolean seeVanished = c.hasPermission(Permissions.VANISH_SEE);

        for (CrownUser user: UserManager.getOnlineUsers()){
            if(user.isVanished() && !seeVanished) continue;
            if(!user.getName().toLowerCase().startsWith(token)) continue;

            Message message = new LiteralMessage(user.getUniqueId().toString());

            builder.suggest(user.getName(), message);
        }

        return builder.buildFuture();
    }

    static CompletableFuture<Suggestions> suggestPlayernamesAndEmotes(CommandContext<CommandSource> c, SuggestionsBuilder builder, boolean ignorePerms){
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ')+1);

        suggestPlayerNames(c.getSource(), builder);
        if(c.getSource().hasPermission(Permissions.DONATOR_3) || ignorePerms){
            return ForTheCrown.getEmotes().getSuggestions(c, builder, ignorePerms);
        }

        return builder.buildFuture();
    }
}