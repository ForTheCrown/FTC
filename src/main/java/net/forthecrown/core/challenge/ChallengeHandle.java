package net.forthecrown.core.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.FTC;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import net.forthecrown.utils.Util;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ChallengeHandle {
    private static final Logger LOGGER = FTC.getLogger();

    private final JsonChallenge challenge;

    public void givePoint(Object playerObject) {
        givePoints(playerObject, 1);
    }

    public void givePoints(Object playerObject, double score) {
        if (hasCompleted(playerObject)) {
            return;
        }

        var player = getPlayer(playerObject);
        var manager = ChallengeManager.getInstance();

        Challenges.apply(challenge, holder -> {
            manager.getOrCreateEntry(player.getUniqueId())
                    .addProgress(holder, (float) score);
        });
    }

    public boolean hasCompleted(Object playerObject) {
        var player = getPlayer(playerObject);
        return Challenges.hasCompleted(challenge, player.getUniqueId());
    }

    static Player getPlayer(Object arg) {
        if (arg instanceof Player player) {
            return player;
        }

        if (arg instanceof UUID uuid) {
            return Objects.requireNonNull(
                    Bukkit.getPlayer(uuid),
                    "Unknown player: " + uuid
            );
        }

        if (arg instanceof String string) {
            return Objects.requireNonNull(
                    Bukkit.getPlayerExact(string),
                    "Unknown player: " + string
            );
        }

        if (arg instanceof User user) {
            user.ensureOnline();
            return user.getPlayer();
        }

        if (arg instanceof CommandSource source) {
            return getPlayer(source.asBukkit());
        }

        throw Util.newException("Expected '%s', found '%s'",
                Player.class.getName(),
                arg == null ? null : arg.getClass().getName()
        );
    }
}