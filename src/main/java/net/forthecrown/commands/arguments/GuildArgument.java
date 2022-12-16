package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;

import java.util.concurrent.CompletableFuture;

public class GuildArgument implements ArgumentType<Guild> {
    @Override
    public Guild parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();

        var name = reader.readUnquotedString();
        var guild = GuildManager.get()
                .getGuild(name);

        if (guild == null) {
            reader.setCursor(start);
            throw Exceptions.unknown("Guild", reader, name);
        }

        return guild;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletionProvider.suggestMatching(builder,
                GuildManager.get().getGuilds()
                        .stream()
                        .map(Guild::getName)
        );
    }
}