package net.forthecrown.challenges;

import static net.forthecrown.command.Exceptions.format;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.user.User;

public interface ChallengeExceptions {

  static CommandSyntaxException nonActiveChallenge(Challenge challenge, User viewer) {
    return format("Challenge {0} is not active!", challenge.displayName(viewer));
  }
}
