package net.forthecrown.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.Crown;
import net.forthecrown.economy.market.Markets;
import net.forthecrown.economy.market.MarketShop;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;

import java.util.concurrent.CompletableFuture;

public class MarketArgument implements ArgumentType<MarketShop>, VanillaMappedArgument {
    public static final DynamicCommandExceptionType UNKNOWN_MARKET = new DynamicCommandExceptionType(o -> new LiteralMessage("Unknown shop: " + o));

    private static final MarketArgument INSTANCE = new MarketArgument();
    private MarketArgument() {}

    public static MarketArgument market() {
        return INSTANCE;
    }

    @Override
    public MarketShop parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        Markets region = Crown.getMarkets();
        MarketShop shop = region.get(name);

        if(shop == null) throw UNKNOWN_MARKET.createWithContext(GrenadierUtils.correctReader(reader, cursor), name);

        return shop;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletionProvider.suggestMatching(builder, Crown.getMarkets().getNames());
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.word();
    }
}