package net.forthecrown.challenges;

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
import net.forthecrown.Loggers;
import net.forthecrown.challenges.event.ChallengeCompleteEvent;
import net.forthecrown.leaderboards.Leaderboards;
import net.forthecrown.registry.Holder;
import net.forthecrown.scripts.Script;
import net.forthecrown.scripts.ScriptLoadException;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.io.JsonWrapper;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

@RequiredArgsConstructor
public class ChallengeEntry {

  public static final String KEY_PROGRESS = "challengeProgress";
  public static final String KEY_STREAKS = "streaks";
  public static final String KEY_COMPLETED = "completed";

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  private final UUID id;
  private final ChallengeManager manager;

  private final Object2FloatMap<String> progress = new Object2FloatOpenHashMap<>();
  private final EnumMap<StreakCategory, Streak> streaks = new EnumMap<>(StreakCategory.class);

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
    var registry = manager.getChallengeRegistry();

    progress.object2FloatEntrySet().removeIf(entry -> {
      return registry.get(entry.getKey())
          .map(Challenge::getResetInterval)
          .filter(entryInterval -> entryInterval == interval)
          .isPresent();
    });

    User user = getUser();
    Component message = ChallengeMessages.challengesReset(interval);

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
    return manager.getChallengeRegistry()
        .getHolderByValue(challenge)
        .map(this::hasCompleted)
        .orElse(false);
  }

  public boolean hasCompleted(Holder<Challenge> holder) {
    return completedKeys.contains(holder.getKey());
  }

  public void complete(Holder<Challenge> holder) {
    completedKeys.add(holder.getKey());
  }

  public void addProgress(Holder<Challenge> holder, float value) {
    var challenge = holder.getValue();

    if (hasCompleted(holder)
        || !Challenges.isActive(challenge)
        || manager.areChallengesLocked()
    ) {
      return;
    }

    float current = getProgress(holder);
    User user = getUser();

    float goal = challenge.getGoal(user);
    float newVal = Math.min(value + current, goal);

    if (newVal >= goal) {
      boolean canComplete = challenge.canComplete(user);

      ChallengeCompleteEvent event = new ChallengeCompleteEvent(user, holder);
      event.setCancelled(!canComplete);
      event.callEvent();

      if (event.isCancelled()) {
        return;
      }

      user.sendMessage(ChallengeMessages.challengeCompleted(challenge, user));

      complete(holder);
      challenge.onComplete(user);

      potentiallyAddStreak(challenge.getStreakCategory());
    }

    progress.put(holder.getKey(), newVal);
  }

  private void potentiallyAddStreak(StreakCategory category) {
    for (var c : manager.getActiveChallenges()) {
      if (c.getValue().getStreakCategory() != category) {
        continue;
      }

      if (!hasCompleted(c)) {
        return;
      }
    }

    var user = getUser();
    user.sendMessage(ChallengeMessages.challengeCategoryFinished(category));

    Streak streak = getStreak(category);
    int streakValue = streak.increase(System.currentTimeMillis());

    var event = new StreakIncreaseEvent(user, category, streakValue, this);
    event.callEvent();

    Leaderboards.updateWithSource("streaks/current/" + category.name().toLowerCase());
    Leaderboards.updateWithSource("streaks/highest/" + category.name().toLowerCase());

    // Find script callbacks for streak increase
    var scripts = manager.getStorage().getScripts(category);

    // If there are scripts to execute
    if (scripts.isEmpty()) {
      return;
    }

    // Run all scripts for each category
    scripts.forEach(sources -> {
      try (Script script = Scripts.newScript(sources)) {
        try {
          script.compile();
        } catch (ScriptLoadException exc) {
          LOGGER.error("Failed to load script {} to trigger streak increase script", script, exc);
          return;
        }

        script.evaluate()
            .flatMapScript(script1 -> {
              return script1.invoke(Challenges.METHOD_STREAK_INCREASE, user, streakValue);
            })
            .logError();
      }
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
      json.addMap(KEY_PROGRESS, progress, Function.identity(), JsonPrimitive::new);
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

    var registry = manager.getChallengeRegistry();

    json.getMap(KEY_PROGRESS, Function.identity(), JsonElement::getAsFloat)
        .forEach((s, aFloat) -> {
          registry.getHolder(s).ifPresentOrElse(holder -> {
            progress.put(holder.getKey(), aFloat.floatValue());
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
    return progress.getFloat(e.getKey());
  }
}