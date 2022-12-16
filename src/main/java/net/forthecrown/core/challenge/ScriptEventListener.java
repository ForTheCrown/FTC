package net.forthecrown.core.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.script2.Script;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.EventExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public class ScriptEventListener implements Listener, EventExecutor {
    private static final Logger LOGGER = FTC.getLogger();

    Script script;

    final ChallengeHandle handle;

    @Override
    public void execute(@NotNull Listener listener,
                        @NotNull Event event
    ) {
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
        if (script != null
                && script.hasMethod(Challenges.METHOD_ON_EVENT)
        ) {
            var res = script.invoke(Challenges.METHOD_ON_EVENT, event, handle);

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
                || !script.hasMethod(Challenges.METHOD_GET_PLAYER)
        ) {
            return getFromEvent(event);
        }

        var runResult = script.invoke(Challenges.METHOD_GET_PLAYER, event);

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

        if (event instanceof PlayerDeathEvent event1) {
            return event1.getPlayer();
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
}