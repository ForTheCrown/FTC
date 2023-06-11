package net.forthecrown.scripts.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.source.Source;
import net.forthecrown.utils.io.source.Sources;

public class ScriptArgument implements ArgumentType<Source> {

  public static final ScriptArgument SCRIPT = new ScriptArgument();

  private ScriptArgument() {
  }

  @Override
  public Source parse(StringReader reader) throws CommandSyntaxException {
    String ftcKey = Arguments.FTC_KEY.parse(reader);

    Path scriptsDir = Scripts.getService().getScriptsDirectory();
    Path file = scriptsDir.resolve(ftcKey);

    if (!Files.exists(file)) {
      throw Exceptions.format("Script file '{0}' does not exist", file);
    }

    return Sources.fromPath(file, scriptsDir);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    Path dir = Scripts.getService().getScriptsDirectory();

    var result = PathUtil.findAllFiles(dir, true, path -> {
      String name = path.getFileName().toString();
      return name.endsWith(".js") || name.endsWith(".ts");
    });

    return Completions.suggest(builder, result);
  }
}