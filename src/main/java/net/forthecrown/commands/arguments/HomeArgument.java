package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.commands.manager.FtcSuggestions;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.forthecrown.royalgrenadier.GrenadierUtils.correctReader;

public class HomeArgument implements ArgumentType<HomeParseResult>, VanillaMappedArgument {
    HomeArgument() {}

    @Override
    public HomeParseResult parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        if (reader.canRead() && reader.peek() == ':') {
            reader.skip();

            String homeName = reader.readUnquotedString();
            var entry = UserManager.get().getUserLookup().get(name);

            if (entry == null) {
                throw Exceptions.unknownUser(reader, cursor, name);
            }

            return new HomeParseResult(correctReader(reader, cursor), entry, homeName);
        }

        return new HomeParseResult(correctReader(reader, cursor), name);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        CommandSource source = (CommandSource) context.getSource();
        List<String> suggestions = new ArrayList<>();

        if (source.hasPermission(Permissions.HOME_OTHERS)) {
            boolean containsDelimiter = remaining.contains(":");
            FtcSuggestions.suggestPlayerNames((CommandSource) context.getSource(), builder, true);

            if (containsDelimiter) {
                String name = remaining.substring(0, remaining.indexOf(':'));
                var entry =  UserManager.get()
                        .getUserLookup()
                        .get(name);

                if (entry != null) {
                    User user = Users.get(entry);
                    user.getHomes().suggestHomeNames(builder, true);
                }
            }
        }

        if (source.isPlayer()) {
            User user = Users.get(source.asOrNull(Player.class));
            user.getHomes().suggestHomeNames(builder, false);
        }

        return CompletionProvider.suggestMatching(builder, suggestions);
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.asList("home", "BotulToxin:home", "Robinoh:nether", "base", "farm");
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.string();
    }
}