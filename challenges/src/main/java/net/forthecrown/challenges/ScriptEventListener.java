package net.forthecrown.challenges;

import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.Loggers;
import net.forthecrown.scripts.Script;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Getter
@RequiredArgsConstructor
public class ScriptEventListener implements Listener, EventExecutor {

  private static final Logger LOGGER = Loggers.getLogger();

  Script script;

  final String[] args;
  final ChallengeHandle handle;

  @Override
  public void execute(@NotNull Listener listener, @NotNull Event event) {
    if (event instanceof Cancellable cancellable
        && cancellable.isCancelled()
    ) {
      return;
    }

    // This apparently doesn't happen automatically ¬_¬
    if (!handle.getChallenge().getEventClass().isInstance(event)) {
      return;
    }

    // Execute script if there's an onEvent method
    if (script != null && script.hasMethod(ScriptedChallenge.METHOD_ON_EVENT)) {
      var res = script.invoke(ScriptedChallenge.METHOD_ON_EVENT, event, handle);

      res.error().ifPresent(throwable -> {
        LOGGER.error("Failed on event {}", event.getClass().getName());
      });

      return;
    }

    // Else, get player and give them a point
    Player player = findPlayer(event);
    if (player == null) {
      return;
    }

    handle.givePoint(player);
  }

  private Player findPlayer(Event event) {
    if (script == null
        || !script.hasMethod(ScriptedChallenge.METHOD_GET_PLAYER)
    ) {
      return getFromEvent(event);
    }

    var runResult = script.invoke(ScriptedChallenge.METHOD_GET_PLAYER, event);

    if (runResult.result().isEmpty()) {
      LOGGER.error(
          "{}: No result returned by getPlayer in script!",
          script
      );

      return null;
    }

    return ChallengeHandle.getPlayer(runResult.result().get());
  }

  private @Nullable Player getFromEvent(Event event) {
    if (event instanceof PlayerEvent event1) {
      return event1.getPlayer();
    }

    if (event instanceof EntityEvent entityEvent
        && entityEvent.getEntity() instanceof Player player
    ) {
      return player;
    }

    LOGGER.error(
        "{}: Cannot execute challenge event! No getPlayer " +
            "method specified in script and event " +
            "is not a player event!",
        script,
        new RuntimeException()
    );

    return null;
  }

  public void consumeScript(Consumer<Script> consumer) {
    if (script == null || !script.isCompiled()) {
      return;
    }

    consumer.accept(script);
  }

  public void reloadScript() {
    if (script.isCompiled()) {
      script.close();
    }

    script.compile();

    if (args != null && args.length > 0) {
      script.put("inputs", args);
    }

    script.put("_challengeHandle", handle);
    script.put("_challenge", handle.getChallenge());

    script.evaluate().throwIfError();
  }

  public void closeScript() {
    if (script == null) {
      return;
    }

    script.close();
    script = null;
  }
}