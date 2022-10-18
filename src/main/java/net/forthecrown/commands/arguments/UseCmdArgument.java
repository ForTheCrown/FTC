package net.forthecrown.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.useables.command.CmdUsables;
import net.forthecrown.useables.command.CommandUsable;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class UseCmdArgument<T extends CommandUsable> implements ArgumentType<T>, VanillaMappedArgument {
    private final Supplier<CmdUsables<T>> manager;
    @Getter
    private final Class<T> typeClass;

    public T get(CommandContext<CommandSource> context, String argumentName) {
        return context.getArgument(argumentName, typeClass);
    }

    public CmdUsables<T> getManager() {
        return manager.get();
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        var cursor = reader.getCursor();
        var name = reader.readUnquotedString();

        var result = manager.get().get(name);

        if (result == null) {
            throw Exceptions.unknown(typeClass.getSimpleName(),
                    GrenadierUtils.correctReader(reader, cursor),
                    name
            );
        }

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return listSuggestions(context, builder, false);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder, boolean ignoreChecks) {
        var source = (CommandSource) context.getSource();
        var manager = this.manager.get();

        if (!source.isPlayer() || ignoreChecks) {
            return CompletionProvider.suggestMatching(builder, manager.keySet());
        }

        var player = source.asOrNull(Player.class);
        var remaining = builder.getRemainingLowerCase();

        for (var u: manager.getUsable(player)) {
            if (!CompletionProvider.startsWith(remaining, u.getName())) {
                continue;
            }

            builder.suggest(u.getName());
        }

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.word();
    }
}