package net.forthecrown.core.commands.brigadier.types.custom;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.enums.Rank;
import net.forthecrown.core.utils.ListUtils;

import java.util.concurrent.CompletableFuture;

public class RankType extends CrownArgType<Rank> {

    private static final RankType RANK = new RankType();
    private RankType(){
        super(obj -> new LiteralMessage("Unknown Rank: " + obj.toString()));
    }

    @Override
    protected Rank parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String rank = reader.readUnquotedString();

        Rank result;
        try {
            result = Rank.valueOf(rank.toUpperCase());
        } catch (IllegalArgumentException e){
            reader.setCursor(cursor);
            throw exception.createWithContext(reader, rank);
        }

        return result;
    }

    public static StringArgumentType rank(){
        return StringArgumentType.word();
    }

    public static <S> Rank getRank(CommandContext<S> c, String argument) throws CommandSyntaxException {
        return RANK.parse(inputToReader(c, argument));
    }

    public static <S> CompletableFuture<Suggestions> suggest(CommandContext<S> context, SuggestionsBuilder builder){
        return CrownCommandBuilder.suggestMatching(builder, ListUtils.arrayToCollection(Rank.values(), r -> r.toString().toLowerCase()));
    }
}
