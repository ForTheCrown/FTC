package net.forthecrown.commands.arguments.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;
import net.minecraft.commands.CommandBuildContext;
import org.bukkit.command.CommandSender;

public class MessageArgument
    implements ArgumentType<MessageArgument.Result>, VanillaMappedArgument
{

  @Override
  public Result parse(StringReader reader) throws CommandSyntaxException {
    var result = new Result(reader.getRemaining());
    reader.setCursor(reader.getTotalLength());

    return result;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return MessageSuggestions.get(context, builder, true);
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.greedyString();
  }

  @RequiredArgsConstructor
  @Getter
  public static class Result {

    private final String text;

    public Component getFormatted() {
      return Text.renderString(text);
    }

    public Component format(CommandSender source) {
      return Text.renderString(source, text);
    }
  }
}