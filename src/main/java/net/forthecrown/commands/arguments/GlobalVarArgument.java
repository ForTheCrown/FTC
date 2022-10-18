package net.forthecrown.commands.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.royalgrenadier.GrenadierUtils;
import net.forthecrown.royalgrenadier.VanillaMappedArgument;
import net.forthecrown.vars.VarData;

import java.util.concurrent.CompletableFuture;

public class GlobalVarArgument implements ArgumentType<VarData<?>>, VanillaMappedArgument {
    @Override
    public VarData<?> parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readUnquotedString();

        VarData result = Crown.getVars().get(name);

        if (result == null) {
            throw Exceptions.unknownVar(GrenadierUtils.correctReader(reader, cursor), name);
        }

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String token = builder.getRemainingLowerCase();

        for (VarData v: Crown.getVars().values()) {
            if (!CompletionProvider.startsWith(token, v.getName())) {
                continue;
            }

            Message tooltip = CmdUtil.toTooltip(v.asComponent());
            builder.suggest(v.getName(), tooltip);
        }

        return builder.buildFuture();
    }

    @Override
    public ArgumentType<?> getVanillaArgumentType() {
        return StringArgumentType.word();
    }
}