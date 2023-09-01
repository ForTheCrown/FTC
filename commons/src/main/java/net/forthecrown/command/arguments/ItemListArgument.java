package net.forthecrown.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.FtcSuggestions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.Suggester;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ArrayArgument;
import net.forthecrown.grenadier.types.ItemArgument;
import net.forthecrown.utils.inventory.ItemList;
import net.forthecrown.utils.inventory.ItemLists;
import org.bukkit.inventory.ItemStack;

public class ItemListArgument implements ArgumentType<ItemListResult> {

  public static final String HELD_ITEM_FLAG = "-held_item";
  public static final String INVENTORY_FLAG = "-inventory";

  private final ArrayArgument<ItemStack> itemsParser;

  public ItemListArgument() {
    var arg = new ItemArgumentType();
    itemsParser = ArgumentTypes.array(arg);
  }

  @Override
  public ItemListResult parse(StringReader reader) throws CommandSyntaxException {
    if (Readers.startsWithArgument(reader, HELD_ITEM_FLAG)) {
      reader.readUnquotedString();
      return ItemListResult.HELD_ITEM;
    }

    if (Readers.startsWithArgument(reader, INVENTORY_FLAG)) {
      reader.readUnquotedString();
      return ItemListResult.INVENTORY;
    }

    var list = itemsParser.parse(reader);
    return new ParsedItemList(ItemLists.newList(list));
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    Completions.suggest(builder, HELD_ITEM_FLAG, INVENTORY_FLAG);
    return itemsParser.listSuggestions(context, builder);
  }

  public static class ParsedItemList implements ItemListResult {

    private final ItemList list;

    public ParsedItemList(ItemList list) {
      this.list = list;
    }

    @Override
    public ItemList get(CommandSource source) throws CommandSyntaxException {
      return list;
    }
  }

  private static class ItemArgumentType implements ArgumentType<ItemStack> {

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
      ItemArgument.Result result = itemArgument.parse(reader);

      suggester = (context, builder) -> {
        builder = builder.createOffset(cursor);
        return itemArgument.listSuggestions(context, builder);
      };

      return result.create(amount, true);
    }

    private Suggester<CommandSource> createInitial() {
      return FtcSuggestions.combined(
          (context, builder) -> {
            return Completions.suggest(builder, "1", "2", "4", "8", "16", "32", "64");
          },
          itemArgument::listSuggestions
      );
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
