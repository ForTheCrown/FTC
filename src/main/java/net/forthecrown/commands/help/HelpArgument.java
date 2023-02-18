package net.forthecrown.commands.help;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.minecraft.commands.arguments.GameProfileArgument;

public class HelpArgument implements ArgumentType<String>, VanillaMappedArgument {

  @Override
  public String parse(StringReader reader) throws CommandSyntaxException {
    char peeked = reader.peek();

    if (StringReader.isQuotedStringStart(peeked)) {
      return reader.readQuotedString();
    }

    int start = reader.getCursor();

    while (reader.canRead() && reader.peek() != ' ') {
      reader.skip();
    }

    return reader.getString()
        .substring(start, reader.getCursor());
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource source)) {
      return builder.buildFuture();
    }

    return FtcHelpMap.getInstance()
        .suggest(source, builder);
  }

  @Override
  public ArgumentType<?> getVanillaArgumentType() {
    return GameProfileArgument.gameProfile();
  }
}