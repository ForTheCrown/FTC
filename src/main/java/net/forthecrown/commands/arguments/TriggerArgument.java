package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.forthecrown.useables.UsableTrigger;
import net.forthecrown.useables.Usables;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ScoreHolderArgument;

public class TriggerArgument
    implements ArgumentType<UsableTrigger>, VanillaMappedArgument
{

  @Override
  public UsableTrigger parse(StringReader reader) throws CommandSyntaxException {
    int cursor = reader.getCursor();
    String name = Arguments.FTC_KEY.parse(reader);
    var trigger = Usables.getInstance().getTriggers().getNamed(name);

    if (trigger == null) {
      throw Exceptions.unknownTrigger(reader, cursor, name);
    }

    return trigger;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                            SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder,
        Usables.getInstance().getTriggers().getNames());
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return ScoreHolderArgument.scoreHolder();
  }
}