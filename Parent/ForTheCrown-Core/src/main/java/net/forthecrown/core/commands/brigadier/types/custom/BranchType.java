package net.forthecrown.core.commands.brigadier.types.custom;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.commands.brigadier.CrownCommandBuilder;
import net.forthecrown.core.enums.Branch;
import net.forthecrown.core.utils.ListUtils;

import java.util.concurrent.CompletableFuture;

public class BranchType extends CrownArgType<Branch> {

    private static final BranchType BRANCH = new BranchType();

    private BranchType() {
        super(obj -> new LiteralMessage("Unkown branch: " + obj.toString()));
    }

    @Override
    protected Branch parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        Branch result;
        try {
            result = Branch.valueOf(name);
        } catch (IllegalArgumentException e){
            reader.setCursor(cursor);
            throw exception.createWithContext(reader, name);
        }

        return result;
    }

    public static StringArgumentType branch(){
        return StringArgumentType.word();
    }

    public static <S> Branch getBranch(CommandContext<S> context, String argument) throws CommandSyntaxException {
        return BRANCH.parse(inputToReader(context, argument));
    }

    public static <S> CompletableFuture<Suggestions> suggest(CommandContext<S> context, SuggestionsBuilder builder){
        return CrownCommandBuilder.suggestMatching(builder, ListUtils.arrayToCollection(Branch.values(), Branch::toString));
    }
}
