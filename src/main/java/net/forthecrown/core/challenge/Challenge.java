package net.forthecrown.core.challenge;

import com.google.common.collect.ImmutableList;
import net.forthecrown.core.FTC;
import net.forthecrown.user.User;
import net.forthecrown.utils.text.Text;
import net.forthecrown.utils.text.writer.TextWriter;
import net.forthecrown.utils.text.writer.TextWriters;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Nullable;

/**
 * Generic interface for challenges
 */
public interface Challenge {

  /**
   * Gets the challenge's name
   */
  Component getName();

  /**
   * List of text's that make the challenge's description
   */
  default ImmutableList<Component> getDescription() {
    return ImmutableList.of();
  }

  /**
   * Gets the challenge's display name.
   * <p>
   * The viewer is required as context for the {@link Reward} info that's displayed in the hover
   * event.
   *
   * @param viewer The user viewing the challenge
   * @return The created display name.
   */
  default Component displayName(@Nullable User viewer) {
    TextWriter writer = TextWriters.newWriter();

    for (Component component : getDescription()) {
      writer.line(component);
    }

    int streak;
    if (viewer != null) {
      streak = ChallengeManager.getInstance()
          .getEntry(viewer.getUniqueId())
          .getStreak(getStreakCategory())
          .get();
    } else {
      streak = 0;
    }

    var reward = getReward();
    if (!reward.isEmpty(streak)) {
      writer.newLine();
      writer.newLine();

      writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
      writer.setFieldValueStyle(Style.style(NamedTextColor.GRAY));

      reward.write(writer, streak);
    }

    var displayName = getName()
        .color(NamedTextColor.YELLOW)
        .hoverEvent(writer.asComponent());

    return replacePlaceholders(displayName, viewer);
  }

  default Component replacePlaceholders(Component component, User user) {
    float goal = getGoal(user);

    return component.replaceText(
        TextReplacementConfig.builder()
            .matchLiteral("%goal")
            .replacement(Text.formatNumber(goal))
            .build()
    );
  }

  /**
   * Gets the reward given to players when they complete the challenge, returns {@link Reward#EMPTY}
   * by default
   */
  default Reward getReward() {
    return Reward.EMPTY;
  }

  /**
   * Gets the interval at which this challenge is reset, by default, this will return
   * {@link ResetInterval#DAILY}
   */
  default ResetInterval getResetInterval() {
    return ResetInterval.DAILY;
  }

  /**
   * Gets the challenge's streak category, used to determine the user's streak for this challenge
   */
  default StreakCategory getStreakCategory() {
    return getResetInterval() == ResetInterval.DAILY
        ? StreakCategory.DAILY
        : StreakCategory.WEEKLY;
  }

  /**
   * Gets the challenge's goal
   */
  StreakBasedValue getGoal();

  /**
   * Gets the effective goal for the given user
   */
  default float getGoal(User user) {
    int streak = ChallengeManager.getInstance()
        .getEntry(user)
        .getStreak(getStreakCategory())
        .get();

    return getGoal().getValue(streak);
  }

  /**
   * Tests if the user can complete this challenge, by default, returns true
   */
  default boolean canComplete(User user) {
    return true;
  }

  /**
   * Challenge completion callback.
   * <p>
   * By default, this will simply give the {@link #getReward()} to the player
   */
  default void onComplete(User user) {
    int streak = ChallengeManager.getInstance()
        .getEntry(user)
        .getStreak(getStreakCategory())
        .get();

    if (getReward().isEmpty(streak)) {
      return;
    }

    getReward().give(user, streak);

    // Find the challenge's entry and then use it's key to log the
    // user completing this challenge
    Challenges.apply(this, holder -> {
      FTC.getLogger().info("{} completed the {} challenge",
          user.getName(), holder.getKey()
      );
    });
  }

  /**
   * Challenge deactivation callback
   */
  void deactivate();

  /**
   * Challenge activation callback
   *
   * @param reset True, if the challenge was just reset, false means the server was restarted,
   *              reloaded or something similar that might require a challenge to be activated again
   *              without being reset.
   * @return Extra info to display in the {@link net.forthecrown.log.DataLog} for activated
   * challenges
   */
  String activate(boolean reset);

  /**
   * Triggers this challenge.
   * <p>
   * <b>Note</b>: The type of valid input for a challenge is defined by its
   * implementation, a valid input for one challenge may be invalid for another.
   *
   * @param input The input to give to the challenge
   */
  void trigger(Object input);
}