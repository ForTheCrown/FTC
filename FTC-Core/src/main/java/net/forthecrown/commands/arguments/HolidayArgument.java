package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.core.ServerHolidays;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;

import java.util.concurrent.CompletableFuture;

public class HolidayArgument implements ArgumentType<ServerHolidays.Holiday>, VanillaMappedArgument {
    private static final HolidayArgument INSTANCE = new HolidayArgument();
    private static final DynamicCommandExceptionType UNKNOWN_HOLIDAY = new DynamicCommandExceptionType(o -> () -> "Unknown holiday: '" + o + "'");

    public static HolidayArgument holiday() {
        return INSTANCE;
    }

    @Override
    public ServerHolidays.Holiday parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String read = reader.readString();

        ServerHolidays.Holiday holiday = Crown.getHolidays().getHoliday(read);

        if (holiday == null) {
            throw UNKNOWN_HOLIDAY.createWithContext(GrenadierUtils.correctReader(reader, cursor), read);
        }

        return holiday;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CompletionProvider.suggestMatching(builder, Crown.getHolidays().getNames());
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.word();
    }
}