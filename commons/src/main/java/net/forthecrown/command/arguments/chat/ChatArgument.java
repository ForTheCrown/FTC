package net.forthecrown.command.arguments.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.command.arguments.chat.MessageArgument.Result;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.text.ChatEmotes;
import net.forthecrown.text.ViewerAwareMessage;
import net.minecraft.commands.CommandBuildContext;

public class ChatArgument
    implements VanillaMappedArgument, ArgumentType<ViewerAwareMessage>
{

  @Override
  public ViewerAwareMessage parse(StringReader reader) throws CommandSyntaxException {
    char peek = reader.canRead() ? reader.peek() : 'a';

    if (peek == '{' || peek == '[' || peek == '"') {
      var result = ArgumentTypes.component().parse(reader);
      return ViewerAwareMessage.wrap(ChatEmotes.format(result));
    }

    if (peek == '\\') {
      reader.skip();
    }

    Result result = Arguments.MESSAGE.parse(reader);
    return result.toPlayerMessage();
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