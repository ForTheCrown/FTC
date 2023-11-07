package net.forthecrown.economy.market.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.economy.EconExceptions;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.SimpleVanillaMapped;

public class MarketArgument implements ArgumentType<MarketShop>, SimpleVanillaMapped {

  private final MarketManager manager;

  public MarketArgument(MarketManager manager) {
    this.manager = manager;
  }

  @Override
  public MarketShop parse(StringReader reader) throws CommandSyntaxException {
    int cursor = reader.getCursor();
    String name = reader.readUnquotedString();

    MarketShop shop = manager.get(name);

    if (shop == null) {
      throw EconExceptions.unknownShop(reader, cursor, name);
    }

    return shop;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder, manager.getNames());
  }

  @Override
  public ArgumentType<?> getVanillaType() {
    return StringArgumentType.word();
  }
}
