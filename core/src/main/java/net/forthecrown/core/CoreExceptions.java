package net.forthecrown.core;

import static net.forthecrown.command.Exceptions.create;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface CoreExceptions {

  CommandSyntaxException CANNOT_IGNORE_SELF = create("You cannot ignore yourself... lol");
}
