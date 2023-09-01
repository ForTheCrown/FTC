package net.forthecrown.guilds.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.Guilds;

public class GuildArgument implements ArgumentType<Guild> {

  @Override
  public Guild parse(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();

    var name = reader.readUnquotedString();
    var guild = Guilds.getManager().getGuild(name);

    if (guild == null) {
      reader.setCursor(start);
      throw Exceptions.unknown("Guild", reader, name);
    }

    return guild;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder,
        Guilds.getManager().getGuilds().stream().map(Guild::getName)
    );
  }
}
