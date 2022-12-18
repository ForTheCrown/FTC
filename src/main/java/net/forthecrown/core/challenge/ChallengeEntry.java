package net.forthecrown.core.challenge;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.script2.Script;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.player.PlayerMoveEvent;

@Getter
@RequiredArgsConstructor
public class ChallengeEntry {

  private static final Logger LOGGER = FTC.getLogger();

  private final UUID id;

  private final Object2FloatMap<Challenge>
      progress = new Object2FloatOpenHashMap<>();

  @Setter
  private int highestStreak = 0;

  /* ------------------------------ METHODS ------------------------------- */

  public User getUser() {
    return Users.get(id);
  }

  public void onReset(ResetInterval interval) {
    progress.object2FloatEntrySet().removeIf(entry -> {
      return entry.getKey()
          .getResetInterval() == interval;
    });

    User user = getUser();
    Component message = Messages.challengesReset(interval);

    if (message != null) {
      user.sendMessage(message);
    }
  }

  public void addProgress(Holder<Challenge> holder, float value) {
    var challenge = holder.getValue();

    if (Challenges.hasCompleted(holder, id)
        || !Challenges.isActive(challenge)
    ) {
      return;
    }

    PlayerMoveEvent
    float current = progress.getFloat(challenge);

    User user = getUser();

    float newVal = Math.min(
        value + current,
        challenge.getGoal(user)
    );

    if (newVal >= challenge.getGoal(user)) {
      if (!challenge.canComplete(user)) {
        return;
      }

      user.sendMessage(
          Messages.challengeCompleted(challenge, user)
      );

      Challenges.logCompletion(holder, id);
      challenge.onComplete(user);

      potentiallyAddStreak(challenge.getStreakCategory());
    }

    progress.put(challenge, newVal);
  }

  private void potentiallyAddStreak(StreakCategory category) {
    var manager = ChallengeManager.getInstance();

    for (var c : manager.getActiveChallenges()) {
      if (c.getStreakCategory() != category) {
        continue;
      }

      if (!Challenges.hasCompleted(c, getId())) {
        return;
      }
    }

    getUser().sendMessage(
        Messages.challengeCategoryFinished(category)
    );

    // Log streak
    Challenges.logStreak(category, id);

    var user = getUser();
    int streak = Challenges.queryStreak(category, user)
        .orElse(1);

    this.highestStreak = Math.max(highestStreak, streak);

    new StreakIncreaseEvent(user, category, streak, this)
        .callEvent();

    // Find script callbacks for streak increase
    var scripts = manager.getStorage().getScripts(category);

    // If there are scripts to execute
    if (scripts.isEmpty()) {
      return;
    }

    // Run all scripts for each category
    scripts.forEach(scriptName -> {
      Script.run(
          scriptName,
          Challenges.METHOD_STREAK_INCREASE,
          user,
          streak
      );
    });
  }
}