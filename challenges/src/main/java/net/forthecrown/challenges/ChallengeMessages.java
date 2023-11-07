package net.forthecrown.challenges;

import static net.forthecrown.text.Text.format;

import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public interface ChallengeMessages {
  static Component challengeCompleted(Challenge challenge, User user) {
    return format("Completed {0} challenge &e{1}&r.",
        NamedTextColor.GRAY,
        challenge.getResetInterval().getDisplayName(),
        challenge.displayName(user)
    );
  }

  static Component challengesReset(ResetInterval interval) {
    if (interval == ResetInterval.MANUAL) {
      return null;
    }

    return format("&6{0}&r challenges have been reset!",
        NamedTextColor.YELLOW,
        interval.getDisplayName()
    );
  }

  static Component challengeCategoryFinished(StreakCategory category) {
    return format("All &6{0}&r challenges complete!",
        NamedTextColor.YELLOW,
        category.getDisplayName()
    );
  }
}
