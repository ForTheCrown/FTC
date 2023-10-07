package net.forthecrown.usables;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.usables.objects.UsableObject;
import org.jetbrains.annotations.NotNull;

public interface ObjectType<T> extends Suggester<CommandSource> {

  T parse(StringReader reader, CommandSource source) throws CommandSyntaxException;

  default T createEmpty() throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  default boolean canApplyTo(UsableObject object) {
    return true;
  }

  <S> DataResult<T> load(Dynamic<S> dynamic);

  <S> DataResult<S> save(@NotNull T value, @NotNull DynamicOps<S> ops);

  @Override
  default CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return Suggestions.empty();
  }
}
