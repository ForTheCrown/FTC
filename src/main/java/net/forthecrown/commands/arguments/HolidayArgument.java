package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.holidays.Holiday;
import net.forthecrown.core.holidays.ServerHolidays;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;

import java.util.concurrent.CompletableFuture;

public class HolidayArgument implements ArgumentType<Holiday>, VanillaMappedArgument {
    @Override
    public Holiday parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String read = reader.readString();

        Holiday holiday = ServerHolidays.get().getHoliday(read);

        if (holiday == null) {
            throw Exceptions.unknownHoliday(
                    GrenadierUtils.correctReader(reader, cursor), read
            );
        }

        return holiday;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletionProvider.suggestMatching(builder, ServerHolidays.get().getNames());
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.word();
    }
}