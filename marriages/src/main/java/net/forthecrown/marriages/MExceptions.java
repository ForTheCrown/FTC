package net.forthecrown.marriages;

import static net.forthecrown.command.Exceptions.create;
import static net.forthecrown.command.Exceptions.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;

public interface MExceptions {

  CommandSyntaxException NO_REPLY_TARGETS = create("No one to reply to.");

  CommandSyntaxException NOT_MARRIED = create("You are not married.");

  CommandSyntaxException ALREADY_MARRIED = create("You are already married.");

  CommandSyntaxException MARRY_SELF = create("Cannot marry yourself");

  CommandSyntaxException MARRY_DISABLED_SENDER = create("You have disabled marriage proposals");

  CommandSyntaxException NO_PROPOSALS = create("You haven't received any proposals");

  CommandSyntaxException PRIEST_ALREADY_ACCEPTED = create("You have already accepted.");

  CommandSyntaxException PRIEST_NO_ONE_WAITING = create("You have no one awaiting marriage.");

  static CommandSyntaxException targetAlreadyMarried(User user, User spouse) {
    return format("{0, user} is already married to {1, user}.", user, spouse);
  }

  static CommandSyntaxException senderAlreadyMarried(User spouse) {
    return format("You are already married to {0, user},", spouse);
  }

  static CommandSyntaxException marriageDisabledTarget(User target) {
    return format("{0, user} has disabled marriage requests.", target);
  }

}
