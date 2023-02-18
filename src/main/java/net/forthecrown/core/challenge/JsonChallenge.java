package net.forthecrown.core.challenge;

import static java.util.Objects.requireNonNull;
import static net.forthecrown.core.challenge.Challenges.METHOD_CAN_COMPLETE;
import static net.forthecrown.core.challenge.Challenges.METHOD_ON_ACTIVATE;
import static net.forthecrown.core.challenge.Challenges.METHOD_ON_COMPLETE;
import static net.forthecrown.core.challenge.Challenges.METHOD_ON_EVENT;
import static net.forthecrown.core.challenge.Challenges.METHOD_ON_RESET;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.core.FTC;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

@Getter
public class JsonChallenge implements Challenge {

  private final Component name;
  private final ImmutableList<Component> description;

  private final Class<Event> eventClass;
  private final String script;

  private final StreakBasedValue goal;

  private final ResetInterval resetInterval;

  private final Reward reward;

  ScriptEventListener listener;
  boolean listenerRegistered = false;

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  private JsonChallenge(Builder builder) {
    this.name = requireNonNull(builder.name, "Name null");
    this.description = requireNonNull(builder.description, "Desc null")
        .build();

    this.eventClass = builder.eventClass;
    this.script = requireNonNull(builder.script, "Script null");

    this.goal = builder.goal;

    this.resetInterval = requireNonNull(builder.resetInterval,
        "Reset interval null"
    );

    this.reward = requireNonNull(builder.reward, "Reward null");
  }

  /* ------------------------------ METHODS ------------------------------- */

  @Override
  public CompletionStage<String> activate(boolean reset) {
    registerListener();

    getListener().consumeScript(s -> {
      s.invokeIfExists(METHOD_ON_ACTIVATE, getListener().getHandle());
    });

    return CompletableFuture.completedFuture("");
  }

  @Override
  public void deactivate() {
    unregisterListener();

    listener.consumeScript(s -> {
      s.invokeIfExists(METHOD_ON_RESET, listener.getHandle());
    });
    listener.closeScript();
  }

  public void registerListener() {
    if (listenerRegistered) {
      return;
    }

    listener.reloadScript(script);

    if (eventClass != null) {
      Bukkit.getPluginManager().registerEvent(
          eventClass,
          listener,
          EventPriority.MONITOR,
          listener,
          FTC.getPlugin(),
          true
      );
    }

    listenerRegistered = true;
  }

  public void unregisterListener() {
    if (!listenerRegistered) {
      return;
    }

    if (eventClass != null) {
      HandlerList.unregisterAll(listener);
    }

    listenerRegistered = false;
  }

  @Override
  public boolean canComplete(User user) {
    if (getListener().getScript() == null) {
      return true;
    }

    if (!getListener().getScript().hasMethod(METHOD_CAN_COMPLETE)) {
      return true;
    }

    var result = getListener().getScript()
        .invoke(METHOD_CAN_COMPLETE, user);

    return result
        .asBoolean()
        .orElse(false);
  }

  @Override
  public void onComplete(User user) {
    listener.consumeScript(s -> {
      s.invokeIfExists(METHOD_ON_COMPLETE, user);
    });

    Challenge.super.onComplete(user);
  }

  @Override
  public void trigger(Object input) {
    if (eventClass == null
        && Strings.isNullOrEmpty(script)
    ) {
      listener.getHandle().givePoint(input);
      return;
    }

    if (getListener().getScript() == null) {
      Loggers.getLogger().error(
          "Cannot manually invoke script {}! No script set",
          getListener().getScript()
      );

      return;
    }

    if (eventClass != null) {
      if (eventClass.isInstance(input)) {
        listener.execute(listener, (Event) input);
        return;
      }

      Loggers.getLogger().error(
          "Cannot manually invoke script {}! Event class has "
              + "been specified!",
          getListener().getScript()
      );

      return;
    }

    if (!getListener().getScript().hasMethod(METHOD_ON_EVENT)) {
      if (input instanceof Player player) {
        listener.getHandle().givePoint(player);
        return;
      }

      Loggers.getLogger().error(
          "Cannot manually invoke script {}! No onEvent method set",
          getListener().getScript()
      );

      return;
    }

    getListener().getScript().invoke(
        METHOD_ON_EVENT,
        input,
        getListener().getHandle()
    );
  }

  /* -------------------------- OBJECT OVERRIDES -------------------------- */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof JsonChallenge challenge)) {
      return false;
    }

    return getGoal() == challenge.getGoal()
        && getName().equals(challenge.getName())
        && getEventClass().equals(challenge.getEventClass())
        && Objects.equals(getScript(), challenge.getScript())
        && getResetInterval() == challenge.getResetInterval();
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getName(),
        getEventClass(),
        getScript(),
        getGoal(),
        getResetInterval()
    );
  }

  /* ------------------------------ BUILDER ------------------------------- */

  public static Builder builder() {
    return new Builder();
  }

  @Setter
  @Getter
  @Accessors(chain = true, fluent = true)
  public static class Builder {

    private Component name;

    private final ImmutableList.Builder<Component>
        description = ImmutableList.builder();

    private Class<Event> eventClass;
    private String script;

    private Reward reward = Reward.EMPTY;
    private ResetInterval resetInterval = ResetInterval.DAILY;

    private StreakBasedValue goal = StreakBasedValue.ONE;

    public Builder addDesc(Component text) {
      description.add(text);
      return this;
    }

    public JsonChallenge build() {
      return new JsonChallenge(this);
    }
  }
}