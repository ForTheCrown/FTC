package net.forthecrown.core.challenge;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.forthecrown.core.FTC;
import net.forthecrown.core.script2.Script;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.io.Results;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;

public class ChallengeParser {
    public static final String
            KEY_NAME = "displayName",
            KEY_DESC = "description",

            KEY_SCRIPT = "script",
            KEY_EVENT_CLASS = "eventClass",

            KEY_RESET_INTERVAL = "type",

            KEY_REWARD = "reward",
            KEY_GOAL = "goal",

            EVENT_CUSTOM = "custom";

    public static DataResult<JsonChallenge> parse(JsonObject object) {
        JsonWrapper json = JsonWrapper.wrap(object);

        if (!json.has(KEY_NAME)) {
            return Results.errorResult("No display name specified");
        }

        JsonChallenge.Builder builder = JsonChallenge.builder()
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
        for (var c: json.getList(KEY_DESC, JsonUtils::readText)) {
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
                Class eventClass = Class.forName(
                        className, true,

                        FTC.getPlugin()
                                .getClass()
                                .getClassLoader()
                );

                if (!Event.class.isAssignableFrom(eventClass)) {
                    return Results.errorResult(
                            "Class '%s' is not a sub class of '%s'",
                            eventClass.getName(), Event.class.getName()
                    );
                }

                builder.eventClass(eventClass);

                if (Strings.isNullOrEmpty(builder.script())
                        && !PlayerEvent.class.isAssignableFrom(eventClass)
                        && eventClass != PlayerDeathEvent.class
                ) {
                    return Results.errorResult(
                            "No script specified and given event (%s) " +
                                    "was not a player event",

                            eventClass.getName()
                    );
                }
            } catch (ClassNotFoundException e) {
                return Results.errorResult(
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

        if (Strings.isNullOrEmpty(builder.script())) {
            return build(builder, null);
        }

        return build(builder, Script.read(builder.script()));
    }

    static DataResult<JsonChallenge> build(JsonChallenge.Builder builder,
                                           Script script
    ) {
        JsonChallenge challenge = builder.build();
        ChallengeHandle handle = new ChallengeHandle(challenge);
        challenge.listener = new ScriptEventListener(script, handle);

        return DataResult.success(challenge);
    }
}