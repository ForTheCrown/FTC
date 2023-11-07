package net.forthecrown.challenges;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

@Getter
public class ChallengeHandle {

  private static final Logger LOGGER = Loggers.getLogger();

  private final ScriptedChallenge challenge;
  private final ChallengeManager manager;

  public ChallengeHandle(ScriptedChallenge challenge) {
    this.challenge = challenge;
    this.manager = Challenges.getManager();
  }

  public void givePoint(Object playerObject) {
    givePoints(playerObject, 1);
  }

  public void givePoints(Object playerObject, double score) {
    if (hasCompleted(playerObject)) {
      return;
    }

    var player = getPlayer(playerObject);

    Challenges.apply(challenge, holder -> {
      manager.getEntry(player.getUniqueId())
          .addProgress(holder, (float) score);
    });
  }

  public boolean hasCompleted(Object playerObject) {
    var player = getPlayer(playerObject);
    var entry = manager.getEntry(player.getUniqueId());

    return entry.hasCompleted(challenge);
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

    throw new IllegalArgumentException(String.format("Expected '%s', found '%s'",
        Player.class.getName(),
        arg == null ? null : arg.getClass().getName()
    ));
  }
}