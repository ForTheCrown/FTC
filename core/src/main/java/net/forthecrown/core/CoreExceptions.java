package net.forthecrown.core;

import static net.forthecrown.command.Exceptions.create;
import static net.forthecrown.command.Exceptions.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;
import org.bukkit.Location;

public interface CoreExceptions {

  CommandSyntaxException CANNOT_IGNORE_SELF = create("You cannot ignore yourself... lol");

  static CommandSyntaxException profilePrivate(User user) {
    return format("{0, user}'s profile is not public.", user);
  }

  static CommandSyntaxException notSign(Location l) {
    return format("{0, location, -c -w} is not a sign", l);
  }
}
