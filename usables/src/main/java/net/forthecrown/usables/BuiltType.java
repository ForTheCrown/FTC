package net.forthecrown.usables;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.usables.objects.UsableObject;
import net.forthecrown.utils.io.TagOps;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuiltType<T> implements UsageType<T> {

  private final Supplier<T> emptyFactory;
  private final Parser<T> parser;
  private final Loader<T> loader;
  private final Saver<T> saver;

  private final Suggester<CommandSource> suggester;

  private final boolean requiresInput;

  private final Predicate<UsableObject> applicableTo;

  private BuiltType(Builder<T> builder) {
    this.emptyFactory = builder.emptyFactory;
    this.parser = builder.parser;
    this.loader = builder.loader;
    this.saver = builder.saver;
    this.suggester = builder.suggester;

    this.applicableTo = builder.applicableTo;

    Objects.requireNonNull(suggester, "No suggester");

    if (emptyFactory == null) {
      Objects.requireNonNull(parser, "Null parser while missing empty constructor");
      Objects.requireNonNull(loader, "Null loader while missing empty constructor");
      Objects.requireNonNull(saver, "Null saver while missing empty constructor");
    }

    if (builder.requiresInput == TriState.NOT_SET) {
      this.requiresInput = emptyFactory == null;
    } else {
      this.requiresInput = builder.requiresInput.toBooleanOrElse(false);
    }
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  @Override
  public boolean canApplyTo(UsableObject object) {
    if (applicableTo == null) {
      return true;
    }
    return applicableTo.test(object);
  }

  @Override
  public T parse(StringReader reader, CommandSource source) throws CommandSyntaxException {
    if (requiresInput && !reader.canRead()) {
      throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
          .dispatcherExpectedArgumentSeparator()
          .createWithContext(reader);
    }

    if (parser == null) {
      return createEmpty();
    }

    return parser.parse(reader, source);
  }

  @Override
  public @NotNull <S> DataResult<T> load(@Nullable Dynamic<S> dynamic) {
    if (loader == null) {
      return DataResult.success(emptyFactory.get());
    }

    return loader.load((Dynamic<Object>) dynamic);
  }

  @Override
  public <S> DataResult<S> save(@NotNull T value, @NotNull DynamicOps<S> ops) {
    if (saver == null) {
      return DataResult.success(ops.empty());
    }

    return (DataResult<S>) saver.save(value, (DynamicOps<Object>) ops);
  }

  @Override
  public CompletableFuture<Suggestions> getSuggestions(
      CommandContext<CommandSource> context,
      SuggestionsBuilder builder
  ) {
    return suggester.getSuggestions(context, builder);
  }

  @Override
  public T createEmpty() throws UnsupportedOperationException {
    if (emptyFactory != null) {
      return emptyFactory.get();
    }
    return UsageType.super.createEmpty();
  }

  public interface Parser<T> {
    T parse(StringReader reader, CommandSource source) throws CommandSyntaxException;
  }

  public interface Loader<T> {
    @NotNull DataResult<T> load(Dynamic<Object> dynamic);
  }

  public interface Saver<T> {
    @Nullable DataResult<Object> save(T value, DynamicOps<Object> ops);
  }

  @Setter
  @Accessors(chain = true, fluent = true)
  public static class Builder<T> {

    Supplier<T> emptyFactory;
    Parser<T> parser;
    Loader<T> loader;
    Saver<T> saver;

    Suggester<CommandSource> suggester = (context, builder) -> Suggestions.empty();

    TriState requiresInput = TriState.NOT_SET;

    Predicate<UsableObject> applicableTo;

    public Builder<T> tagLoader(Function<BinaryTag, T> loader) {
      return loader(dynamic -> {
        BinaryTag tag;
        DynamicOps<?> ops = dynamic.getOps();

        if (ops instanceof TagOps) {
          tag = (BinaryTag) dynamic.getValue();
        } else {
          tag = dynamic.getOps().convertTo(TagOps.OPS, dynamic.getValue());
        }

        return DataResult.success(loader.apply(tag));
      });
    }

    public Builder<T> tagSaver(Function<T, BinaryTag> saver) {
      return saver((value, ops) -> {
        BinaryTag tag = saver.apply(value);

        if (Objects.equals(ops, TagOps.OPS)) {
          return DataResult.success(tag);
        } else {
          Object o = TagOps.OPS.convertTo(ops, tag);
          return DataResult.success(o);
        }
      });
    }

    public BuiltType<T> build() {
      return new BuiltType<>(this);
    }
  }
}
