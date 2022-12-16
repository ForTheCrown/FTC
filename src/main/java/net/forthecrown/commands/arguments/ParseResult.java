package net.forthecrown.commands.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;

public interface ParseResult<R> {
    R get(CommandSource source, boolean validate) throws CommandSyntaxException;
}