package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.FtcSuggestionProvider;
import net.forthecrown.core.chat.FtcFormatter;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

public class ChatArgument implements ArgumentType<Component>, VanillaMappedArgument {
    private static final ChatArgument INSTANCE = new ChatArgument();

    public static ChatArgument chat() {
        return INSTANCE;
    }

    @Override
    public Component parse(StringReader reader) throws CommandSyntaxException {
        char peek = reader.peek();

        if(peek == '{' || peek == '[' || peek == '"') {
            return ComponentArgument.component().parse(reader);
        }

        String all = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());

        return FtcFormatter.formatString(all);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemainingLowerCase();

        if (remaining.isBlank() || remaining.startsWith("{")) {
            CompletionProvider.suggestMatching(builder,
                    "{\"text\":\"\",\"italic\":false,\"color\":\"white\"}",
                    "{\"text\":\"\"}",
                    "{}", "[]"
            );

            FtcSuggestionProvider.__suggestPlayerNamesAndEmotes((CommandContext<CommandSource>) context, builder, true);
            return builder.buildFuture();
        }

        return FtcSuggestionProvider.suggestPlayerNamesAndEmotes((CommandContext<CommandSource>) context, builder, true);
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.greedyString();
    }
}