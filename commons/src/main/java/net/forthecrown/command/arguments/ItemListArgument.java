package net.forthecrown.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Readers;
import net.forthecrown.grenadier.types.ArgumentTypes;
import net.forthecrown.grenadier.types.ArrayArgument;
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

}
