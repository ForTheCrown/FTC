package net.forthecrown.core;

import static net.forthecrown.command.Exceptions.create;
import static net.forthecrown.command.Exceptions.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;

public interface CoreExceptions {

  CommandSyntaxException CANNOT_IGNORE_SELF = create("You cannot ignore yourself... lol");

  static CommandSyntaxException profilePrivate(User user) {
    return format("{0, user}'s profile is not public.", user);
  }
}
