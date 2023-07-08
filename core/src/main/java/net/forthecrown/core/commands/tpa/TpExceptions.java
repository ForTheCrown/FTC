package net.forthecrown.core.commands.tpa;

import static net.forthecrown.command.Exceptions.create;
import static net.forthecrown.command.Exceptions.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;

public interface TpExceptions {

  CommandSyntaxException CANNOT_TP = create("Cannot currently teleport.");

  CommandSyntaxException NO_TP_REQUESTS = create("You don't have any tp requests.");

  CommandSyntaxException CANNOT_TP_SELF = create("You cannot teleport to yourself.");

  CommandSyntaxException NOT_CURRENTLY_TELEPORTING = create("You aren't currently teleporting");

  CommandSyntaxException TPA_DISABLED_SENDER = create(
      "You have TPA requests disabled.\nUse /tpatoggle to enable them.");

  CommandSyntaxException CANNOT_TP_HERE = create("Cannot tpa here.");

  static CommandSyntaxException tpaDisabled(User user) {
    return format("{0, user} has disabled TPA requests.", user);
  }

  static CommandSyntaxException cannotTpaTo(User user) {
    return format("Cannot tpa to {0, user}.", user);
  }
}
