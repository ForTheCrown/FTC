package net.forthecrown.command.arguments.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.ChatEmotes;
import net.forthecrown.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandBuildContext;

public class ChatArgument
    implements VanillaMappedArgument, ArgumentType<Component>
{

  @Override
  public Component parse(StringReader reader) throws CommandSyntaxException {
    char peek = reader.peek();

    if (peek == '{' || peek == '[' || peek == '"') {
      var result = ArgumentTypes.component().parse(reader);
      return ChatEmotes.format(result);
    }

    if (peek == '\\') {
      reader.skip();
    }

    var remaining = reader.getRemaining();
    reader.setCursor(reader.getTotalLength());

    return Text.renderString(remaining);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                            SuggestionsBuilder builder
  ) {
    return MessageSuggestions.get(context, builder, true);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return Arguments.MESSAGE.getVanillaType(context);
  }
}