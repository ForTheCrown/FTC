package net.forthecrown.commands.usables;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.UsageTypeHolder;

public interface UsageHolderProvider<T extends UsageTypeHolder> {
    T get(CommandContext<CommandSource> context) throws CommandSyntaxException;
}