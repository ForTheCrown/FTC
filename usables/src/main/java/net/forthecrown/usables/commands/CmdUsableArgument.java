package net.forthecrown.usables.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.SimpleVanillaMapped;
import net.forthecrown.usables.CmdUsables;
import net.forthecrown.usables.objects.CommandUsable;
import org.bukkit.entity.Player;

public class CmdUsableArgument<T extends CommandUsable>
    implements ArgumentType<T>, SimpleVanillaMapped
{

  private final CmdUsables<T> usables;
  private final String name;

  public CmdUsableArgument(CmdUsables<T> usables, String name) {
    this.usables = usables;
    this.name = name;
  }

  @Override
  public T parse(StringReader reader) throws CommandSyntaxException {
    final int start = reader.getCursor();

    String key = Arguments.FTC_KEY.parse(reader);
    T usable = usables.get(key);

    if (usable == null) {
      reader.setCursor(start);
      throw Exceptions.unknown(name, reader, key);
    }

    return usable;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource source) || !source.isPlayer()) {
      return Suggestions.empty();
    }

    Player player = source.asPlayerOrNull();
    return Completions.suggest(builder, usables.getUsable(player).stream().map(CommandUsable::getName));
  }

  @Override
  public ArgumentType<?> getVanillaType() {
    return Arguments.FTC_KEY.getVanillaType();
  }
}
