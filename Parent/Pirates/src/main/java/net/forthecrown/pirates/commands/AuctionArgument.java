package net.forthecrown.pirates.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.pirates.auctions.Auction;
import net.forthecrown.pirates.auctions.AuctionManager;
import net.forthecrown.royalgrenadier.GrenadierUtils;

import java.util.concurrent.CompletableFuture;

public class AuctionArgument implements ArgumentType<Auction> {
    private AuctionArgument () {}
    public static final AuctionArgument AUCTION = new AuctionArgument();

    public static final DynamicCommandExceptionType UNKNOWN_AUCTION = new DynamicCommandExceptionType(o -> () -> "Unknown Auction: " + o);

    public static AuctionArgument auction(){
        return AUCTION;
    }

    @Override
    public Auction parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        Auction auction = AuctionManager.getAuction(name);
        if(auction == null) throw UNKNOWN_AUCTION.createWithContext(GrenadierUtils.correctCursorReader(reader, cursor), name);

        return auction;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletionProvider.suggestMatching(builder, AuctionManager.getAuctionNames());
    }
}
