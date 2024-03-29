package net.forthecrown.challenges;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.forthecrown.utils.Result;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;

public class ScriptedChallengeLoader {

  static final String KEY_NAME = "displayName";
  static final String KEY_DESC = "description";
  static final String KEY_SCRIPT = "script";
  static final String KEY_EVENT_CLASS = "eventClass";
  static final String KEY_RESET_INTERVAL = "type";
  static final String KEY_REWARD = "reward";
  static final String KEY_GOAL = "goal";
  static final String KEY_ARGS = "inputs";
  static final String EVENT_CUSTOM = "custom";

  public static Result<ScriptedChallenge> load(JsonObject object) {
    JsonWrapper json = JsonWrapper.wrap(object);

    if (!json.has(KEY_NAME)) {
      return Result.error("No display name specified");
    }

    ScriptedChallenge.Builder builder = ScriptedChallenge.builder()
        .name(json.getComponent(KEY_NAME))
        .script(json.getString(KEY_SCRIPT, ""))

        .goal(StreakBasedValue.read(
            json.get(KEY_GOAL),
            StreakBasedValue.ONE
        ))

        .resetInterval(json.getEnum(
            KEY_RESET_INTERVAL,
            ResetInterval.class,
            ResetInterval.DAILY
        ));

    // No need to test if object contains description, getList returns
    // an empty list if it doesn't
    for (var c : json.getList(KEY_DESC, JsonUtils::readText)) {
      builder.addDesc(c);
    }

    // Name of the event class this challenge will listen to,
    // will be EVENT_CUSTOM, empty or null, if it expects to
    // be called from within FTC code and not listen to a
    // bukkit event
    String className = json.getString(KEY_EVENT_CLASS);

    if (!Strings.isNullOrEmpty(className)
        && !className.equalsIgnoreCase(EVENT_CUSTOM)
    ) {
      try {
        @SuppressWarnings("rawtypes")
        Class eventClass = Class.forName(className, true, ScriptedChallengeLoader.class.getClassLoader());

        if (!Event.class.isAssignableFrom(eventClass)) {
          return Result.error(
              "Class '%s' is not a subclass of '%s'",
              eventClass.getName(), Event.class.getName()
          );
        }

        builder.eventClass(eventClass);

        if (Strings.isNullOrEmpty(builder.script())
            && !PlayerEvent.class.isAssignableFrom(eventClass)
            && eventClass != PlayerDeathEvent.class
        ) {
          return Result.error(
              "No script specified and given event (%s) " +
                  "was not a player event",

              eventClass.getName()
          );
        }
      } catch (ClassNotFoundException e) {
        return Result.error(
            "Class '%s' not found",
            className
        );
      }
    }

    if (json.has(KEY_REWARD)) {
      builder.reward(
          Reward.deserialize(json.get(KEY_REWARD))
      );
    }

    String[] args = null;
    if (json.has(KEY_ARGS)) {
      args = json.getArray(KEY_ARGS, JsonElement::getAsString, String[]::new);
    }

    return build(builder, args);
  }

  static Result<ScriptedChallenge> build(ScriptedChallenge.Builder builder, String[] args) {
    ScriptedChallenge challenge = builder.build();
    ChallengeHandle handle = new ChallengeHandle(challenge);
    challenge.listener = new ScriptEventListener(args, handle);

    return Result.success(challenge);
  }
}