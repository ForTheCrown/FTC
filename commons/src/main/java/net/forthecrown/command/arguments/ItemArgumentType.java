package net.forthecrown.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ItemArgument;
import org.bukkit.inventory.ItemStack;

public class ItemArgumentType implements ArgumentType<ItemStack> {

  @Override
  public ItemStack parse(StringReader reader) throws CommandSyntaxException {
    return new ItemStackParser(reader).parse();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    if (!(context.getSource() instanceof CommandSource)) {
      return Suggestions.empty();
    }

    StringReader reader = Readers.forSuggestions(builder);
    ItemStackParser parser = new ItemStackParser(reader);

    try {
      parser.parse();
    } catch (CommandSyntaxException ignored) {
      // Ignored, we just need the parser to get the suggestions ready
    }

    return parser.getSuggestions((CommandContext<CommandSource>) context, builder);
  }

  private static class ItemStackParser implements Suggester<CommandSource> {

    private final ItemArgument itemArgument;
    private final StringReader reader;

    private Suggester<CommandSource> suggester;

    public ItemStackParser(StringReader reader) {
      this.reader = reader;
      this.itemArgument = ArgumentTypes.item();
    }

    public ItemStack parse() throws CommandSyntaxException {
      suggester = createInitial();

      if (!reader.canRead()) {
        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
            .readerExpectedInt()
            .createWithContext(reader);
      }

      int amount = 1;
      if (StringReader.isAllowedNumber(reader.peek())) {
        amount = Readers.readPositiveInt(reader, 1, 64);
        int cursor = reader.getCursor();

        suggester = (context, builder) -> {
          builder = builder.createOffset(cursor);
          return Completions.suggest(builder, ";");
        };

        reader.expect(';');
      } else {
        amount = 1;
      }

      int cursor = reader.getCursor();
      suggester = (context, builder) -> {
        builder = builder.createOffset(cursor);
        return itemArgument.listSuggestions(context, builder);
      };

      ItemArgument.Result result = itemArgument.parse(reader);
      return result.create(amount, true);
    }

    private Suggester<CommandSource> createInitial() {
      SuggestionProvider<CommandSource> combined = Completions.combine(
          (context, builder) -> {
            return Completions.suggest(builder, "1", "2", "4", "8", "16", "32", "64");
          },
          itemArgument::listSuggestions
      );

      return Suggester.wrap(combined);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
        CommandContext<CommandSource> context,
        SuggestionsBuilder builder
    ) {
      if (suggester == null) {
        suggester = createInitial();
      }

      return suggester.getSuggestions(context, builder);
    }
  }
}
