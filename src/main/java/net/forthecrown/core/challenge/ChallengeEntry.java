package net.forthecrown.core.challenge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.EnumMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Messages;
import net.forthecrown.core.registry.Holder;
import net.forthecrown.core.script2.Script;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;

@RequiredArgsConstructor
public class ChallengeEntry {
  public static final String
      KEY_PROGRESS = "challengeProgress",
      KEY_STREAKS = "streaks",
      KEY_COMPLETED = "completed";

  private static final Logger LOGGER = FTC.getLogger();

  @Getter
  private final UUID id;

  private final Object2FloatMap<Holder<Challenge>>
      progress = new Object2FloatOpenHashMap<>();

  private final EnumMap<StreakCategory, Streak>
      streaks = new EnumMap<>(StreakCategory.class);

  /** Set of the keys of every completed challenge */
  private final Set<String> completedKeys = new ObjectOpenHashSet<>();

  /* ------------------------------ METHODS ------------------------------- */

  public Streak getStreak(StreakCategory category) {
    return streaks.computeIfAbsent(category, Streak::new);
  }

  public User getUser() {
    return Users.get(id);
  }

  public void onReset(ResetInterval interval, ChallengeManager manager) {
    progress.object2FloatEntrySet().removeIf(entry -> {
      return entry.getKey().getValue().getResetInterval() == interval;
    });

    User user = getUser();
    Component message = Messages.challengesReset(interval);

    if (message != null) {
      user.sendMessage(message);
    }

    streaks.values().forEach(streak -> {
      if (!streak.getCategory().causesReset(interval)) {
        return;
      }

      streak.reset();
    });

    completedKeys.removeIf(key -> {
      var opt = manager.getChallengeRegistry().get(key).map(challenge -> {
        return challenge.getResetInterval() == interval;
      });

      return opt.orElse(false);
    });
  }

  public boolean hasCompleted(Challenge challenge) {
    return ChallengeManager.getInstance()
        .getChallengeRegistry()
        .getHolderByValue(challenge)
        .map(this::hasCompleted)
        .orElse(false);
  }

  public boolean hasCompleted(Holder<Challenge> holder) {
    return completedKeys.contains(holder.getKey());
  }

  public void complete(Holder<Challenge> holder) {
    Challenges.logCompletion(holder, id);
    completedKeys.add(holder.getKey());
  }

  public void addProgress(Holder<Challenge> holder, float value) {
    var challenge = holder.getValue();

    if (hasCompleted(holder)
        || !Challenges.isActive(challenge)
        || ChallengeManager.getInstance().areChallengesLocked()
    ) {
      return;
    }

    float current = progress.getFloat(holder);
    User user = getUser();

    float newVal = Math.min(
        value + current,
        challenge.getGoal(user)
    );

    if (newVal >= challenge.getGoal(user)) {
      LOGGER.debug("{} is at/over goal ({}) for {}",
          user,
          challenge.getGoal(user),
          holder.getKey()
      );

      if (!challenge.canComplete(user)) {
        return;
      }

      user.sendMessage(
          Messages.challengeCompleted(challenge, user)
      );

      complete(holder);
      challenge.onComplete(user);

      potentiallyAddStreak(challenge.getStreakCategory());
    }

    LOGGER.debug("Set progress of {} to {} for {}",
        holder.getKey(), newVal, user
    );

    progress.put(holder, newVal);
  }

  private void potentiallyAddStreak(StreakCategory category) {
    var manager = ChallengeManager.getInstance();

    for (var c : manager.getActiveChallenges()) {
      if (c.getValue().getStreakCategory() != category) {
        continue;
      }

      if (!hasCompleted(c)) {
        return;
      }
    }

    var user = getUser();
    user.sendMessage(
        Messages.challengeCategoryFinished(category)
    );

    // Log streak
    Challenges.logStreak(category, id);

    Streak streak = getStreak(category);
    int streakValue = streak.increase(System.currentTimeMillis());

    new StreakIncreaseEvent(user, category, streakValue, this)
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
          streakValue
      );
    });
  }

  public void clear() {
    progress.clear();
    completedKeys.clear();
    streaks.clear();
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void serialize(JsonWrapper json) {
    if (!progress.isEmpty()) {
      json.addMap(
          KEY_PROGRESS,
          progress,
          Holder::getKey,
          JsonPrimitive::new
      );
    }

    if (!completedKeys.isEmpty()) {
      json.addList(KEY_COMPLETED, completedKeys, JsonPrimitive::new);
    }

    if (!streaks.isEmpty()) {
      JsonWrapper streakJson = JsonWrapper.create();

      streaks.values().forEach(streak -> {
        JsonObject obj = streak.serialize();

        if (obj.size() == 0) {
          return;
        }

        streakJson.add(streak.getCategory().name().toLowerCase(), obj);
      });

      if (!streakJson.isEmpty()) {
        json.add(KEY_STREAKS, streakJson);
      }
    }
  }

  public void deserialize(JsonWrapper json) {
    clear();

    if (json == null || json.isEmpty()) {
      return;
    }

    var manager = ChallengeManager.getInstance();
    var registry = manager.getChallengeRegistry();


    json.getMap(KEY_PROGRESS, Function.identity(), JsonElement::getAsFloat)
        .forEach((s, aFloat) -> {
          registry.getHolder(s).ifPresentOrElse(holder -> {
            progress.put(holder, aFloat.floatValue());
          }, () -> {
            LOGGER.warn(
                "Unknown challenge {} found in progress data of {}",
                s, getId()
            );
          });
        });

    json.getWrappedNonNull(KEY_STREAKS).entrySet().forEach(entry -> {
      StreakCategory category
          = StreakCategory.valueOf(entry.getKey().toUpperCase());

      var streak = getStreak(category);
      streak.deserialize(entry.getValue().getAsJsonObject());
    });

    completedKeys.addAll(json.getList(KEY_COMPLETED, JsonElement::getAsString));
  }

  public float getProgress(Holder<Challenge> e) {
    return progress.getFloat(e);
  }
}