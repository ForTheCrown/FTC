package net.forthecrown.command.arguments;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.grenadier.CommandSource;

@FunctionalInterface
public interface ParseResult<R> {

  R get(CommandSource source, boolean validate) throws CommandSyntaxException;
}