package net.forthecrown.scripts.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.Exceptions;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
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
    if (Readers.startsWithIgnoreCase(reader, "url=")) {
      reader.setCursor(reader.getCursor() + 4);

      final int start = reader.getCursor();
      String urlString = Readers.readUntilWhitespace(reader);

      try {
        return Sources.fromUrl(urlString);
      } catch (MalformedURLException exc) {
        reader.setCursor(start);
        throw Exceptions.formatWithContext("Invalid URL '{0}'", reader, urlString);
      }
    }

    if (Readers.startsWithIgnoreCase(reader, "raw=")) {
      reader.setCursor(reader.getCursor() + 4);
      String str = reader.getRemaining();
      reader.setCursor(reader.getTotalLength());

      return Sources.direct(str);
    }

    String ftcKey = Arguments.FTC_KEY.parse(reader);
    String pathString;

    if (ftcKey.endsWith(".js")) {
      pathString = ftcKey;
    } else {
      pathString = ftcKey + ".js";
    }

    Path scriptsDir = Scripts.getService().getScriptsDirectory();
    Path file = scriptsDir.resolve(pathString);

    if (!Files.exists(file)) {
      throw Exceptions.format("Script file '{0}' does not exist", ftcKey);
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