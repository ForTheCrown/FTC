package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.script2.ScriptManager;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.GameProfileArgument;

public class ScriptArgument implements ArgumentType<String>, VanillaMappedArgument {

  @Override
  public String parse(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();
    var str = Arguments.FTC_KEY.parse(reader);

    if (!ScriptManager.getInstance().isExistingScript(str)) {
      reader.setCursor(start);
      throw Exceptions.unknown("Script", reader, str);
    }

    return str;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                            SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder,
        ScriptManager.getInstance().findExistingScripts());
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return GameProfileArgument.gameProfile();
  }
}