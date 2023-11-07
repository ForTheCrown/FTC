package net.forthecrown.usables.scripts;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.scripts.commands.ScriptArgument;
import net.forthecrown.usables.ObjectType;
import net.forthecrown.utils.io.source.Source;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptType implements ObjectType<ScriptInstance> {

  @Override
  public ScriptInstance parse(StringReader reader, CommandSource cmdSource)
      throws CommandSyntaxException
  {
    Source source = ScriptArgument.SCRIPT.parse(reader);
    String[] args = ArrayUtils.EMPTY_STRING_ARRAY;

    reader.skipWhitespace();

    if (reader.canRead()) {
      String remaining = reader.getRemaining().trim();
      reader.setCursor(reader.getTotalLength());
      args = remaining.split(" ");
    }

    return new ScriptInstance(source, args);
  }

  @Override
  public @NotNull <S> DataResult<ScriptInstance> load(@Nullable Dynamic<S> dynamic) {
    return dynamic.get("script")
        .flatMap(dynamic1 -> Scripts.loadScriptSource(dynamic1, false))
        .map(source1 -> {
          String[] args = dynamic.get("args")
              .asList(dynamic1 -> dynamic1.asString(""))
              .toArray(String[]::new);

          return new ScriptInstance(source1, args);
        });
  }

  @Override
  public <S> DataResult<S> save(@NotNull ScriptInstance value, @NotNull DynamicOps<S> ops) {
    var mapBuilder = ops.mapBuilder();
    mapBuilder.add("script", value.getSource().save(ops));

    var args = ops.createList(Arrays.stream(value.getArgs()).map(ops::createString));
    mapBuilder.add("args", args);

    return mapBuilder.build(ops.empty());
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return ScriptArgument.SCRIPT.listSuggestions(context, builder);
  }
}
