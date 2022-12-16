package net.forthecrown.dungeons.level;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.dungeons.DungeonWorld;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import net.forthecrown.events.Events;
import net.forthecrown.useables.TriggerManager;
import net.forthecrown.user.Users;
import net.forthecrown.utils.math.Bounds3i;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

@Getter
@RequiredArgsConstructor
public class LevelListener implements Listener {
    private final DungeonLevel level;
    boolean registered;

    boolean inLevel(Location loc) {
        return loc.getWorld().equals(DungeonWorld.get())
                && level.getChunkMap().getTotalArea().contains(loc);
    }

    void register() {
        if (registered) {
            return;
        }

        Events.register(this);
        registered = true;
    }

    void unregister() {
        if (!registered) {
            return;
        }

        Events.unregister(this);
        registered = false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!inLevel(event.getFrom())
                || event.getPlayer().getGameMode() == GameMode.SPECTATOR
        ) {
            return;
        }

        Bounds3i origin = TriggerManager.makePlayerBounds(event.getFrom());
        Bounds3i destination = TriggerManager.makePlayerBounds(event.getTo());
        Bounds3i totalArea = origin.combine(destination);

        var pieces = level.getIntersecting(totalArea);
        pieces.removeIf(piece -> piece instanceof DungeonGate);

        if (pieces.isEmpty()) {
            return;
        }

        var user = Users.get(event.getPlayer());

        for (var p: pieces) {
            if (!p.isTicked()) {
                continue;
            }

            var bounds = p.getBounds();

            boolean originInside = bounds.overlaps(origin);
            boolean destInside = bounds.overlaps(destination);

            // If did not leave or enter the room
            if (originInside == destInside) {
                continue;
            }

            // If exiting
            // Because of the above check, the two booleans
            // must have an opposite state
            if (originInside) {
                p.getUsers().remove(user);

                // If room is now empty
                if (p.getUsers().isEmpty()) {
                    level.getActivePieces().remove(p);
                    level.getInactivePieces().add(p);
                }

                p.onExit(user, level);
            } else {
                level.getActivePieces().add(p);
                level.getInactivePieces().remove(p);

                p.onEnter(user, level);
            }
        }
    }
}