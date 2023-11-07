package net.forthecrown.challenges;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.Loggers;
import net.forthecrown.scripts.ExecResults;
import net.forthecrown.scripts.Scripts;
import net.forthecrown.text.UserClickCallback;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

@Getter
public class ScriptedChallenge implements Challenge {

  public static final String METHOD_ON_RESET = "onReset";
  public static final String METHOD_ON_ACTIVATE = "onActivate";
  public static final String METHOD_CAN_COMPLETE = "canComplete";
  public static final String METHOD_ON_COMPLETE = "onComplete";
  public static final String METHOD_GET_PLAYER = "getPlayer";
  public static final String METHOD_ON_EVENT = "onEvent";

  private final Component name;
  private final ImmutableList<Component> description;

  private final Class<Event> eventClass;
  private final String script;

  private final StreakBasedValue goal;

  private final ResetInterval resetInterval;

  private final Reward reward;

  private final ClickEvent triggerClickEvent;

  ScriptEventListener listener;
  boolean listenerRegistered = false;

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  private ScriptedChallenge(Builder builder) {
    this.name = requireNonNull(builder.name, "Name null");
    this.description = requireNonNull(builder.description, "Desc null").build();

    this.eventClass = builder.eventClass;
    this.script = requireNonNull(builder.script, "Script null");

    this.goal = builder.goal;

    this.resetInterval = requireNonNull(builder.resetInterval, "Reset interval null");
    this.reward = requireNonNull(builder.reward, "Reward null");

    this.triggerClickEvent = ClickEvent.callback((UserClickCallback) this::trigger);
  }

  /* ------------------------------ METHODS ------------------------------- */

  @Override
  public CompletionStage<String> activate(boolean reset) {
    registerListener();

    getListener().consumeScript(s -> {
      if (!s.hasMethod(METHOD_ON_ACTIVATE)) {
        return;
      }

      s.invoke(METHOD_ON_ACTIVATE, getListener().getHandle())
          .logError();
    });

    return CompletableFuture.completedFuture("");
  }

  @Override
  public void deactivate() {
    unregisterListener();

    listener.consumeScript(s -> {
      if (!s.hasMethod(METHOD_ON_RESET)) {
        return;
      }

      s.invoke(METHOD_ON_RESET, listener.getHandle())
          .logError();
    });
    listener.closeScript();
  }

  public void registerListener() {
    if (listenerRegistered) {
      return;
    }

    if (listener.script == null) {
      listener.script = Scripts.fromScriptFile(script);
    }

    listener.reloadScript();

    if (eventClass != null) {
      Bukkit.getPluginManager().registerEvent(
          eventClass,
          listener,
          EventPriority.MONITOR,
          listener,
          Challenges.getPlugin(),
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
        .invoke(METHOD_CAN_COMPLETE, user)
        .logError();

    return ExecResults.toBoolean(result).result().orElse(false);
  }

  @Override
  public void onComplete(User user) {
    listener.consumeScript(s -> {
      if (!s.hasMethod(METHOD_ON_COMPLETE)) {
        return;
      }

      s.invoke(METHOD_ON_COMPLETE, user)
          .logError();
    });

    Challenge.super.onComplete(user);
  }

  @Override
  public void trigger(Object input) {
    if (eventClass == null && Strings.isNullOrEmpty(script)) {
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
          "Cannot manually invoke script {}! Event class has been specified!",
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

    getListener().getScript().invoke(METHOD_ON_EVENT, input, getListener().getHandle())
        .logError();
  }

  /* -------------------------- OBJECT OVERRIDES -------------------------- */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ScriptedChallenge challenge)) {
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

    public ScriptedChallenge build() {
      return new ScriptedChallenge(this);
    }
  }
}