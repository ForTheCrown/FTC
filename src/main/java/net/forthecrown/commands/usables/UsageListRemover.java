package net.forthecrown.commands.usables;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.useables.UsageInstance;
import net.forthecrown.useables.UsageTypeList;

interface UsageListRemover<T extends UsageInstance> {
    void remove(UsageTypeList<T> list, CommandContext<CommandSource> c) throws CommandSyntaxException;
}