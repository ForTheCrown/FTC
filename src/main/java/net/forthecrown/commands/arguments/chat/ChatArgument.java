package net.forthecrown.commands.arguments.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.arguments.Arguments;
import net.forthecrown.grenadier.types.ComponentArgument;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.utils.text.ChatEmotes;
import net.forthecrown.utils.text.Text;
import net.kyori.adventure.text.Component;

import java.util.concurrent.CompletableFuture;

public class ChatArgument implements VanillaMappedArgument, ArgumentType<Component> {
    @Override
    public Component parse(StringReader reader) throws CommandSyntaxException {
        char peek = reader.peek();

        if (peek == '{' || peek == '[' || peek == '"') {
            var result = ComponentArgument.component().parse(reader);
            return ChatEmotes.format(result);
        }

        var remaining = reader.getRemaining();
        reader.setCursor(reader.getTotalLength());

        return Text.renderString(remaining);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return MessageSuggestions.get(context, builder, true);
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return Arguments.MESSAGE.getVanillaArgumentType();
    }
}