package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.economy.Economy;
import net.forthecrown.economy.market.MarketManager;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.internal.VanillaMappedArgument;
import net.minecraft.commands.CommandBuildContext;

public class MarketArgument implements ArgumentType<MarketShop>, VanillaMappedArgument {

  MarketArgument() {
  }

  @Override
  public MarketShop parse(StringReader reader) throws CommandSyntaxException {
    int cursor = reader.getCursor();
    String name = reader.readUnquotedString();

    MarketManager region = Economy.get().getMarkets();
    MarketShop shop = region.get(name);

    if (shop == null) {
      throw Exceptions.unknownShop(reader, cursor, name);
    }

    return shop;
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
                                                            SuggestionsBuilder builder
  ) {
    return Completions.suggest(builder, Economy.get().getMarkets().getNames());
  }

  @Override
  public ArgumentType<?> getVanillaType(CommandBuildContext context) {
    return StringArgumentType.word();
  }
}