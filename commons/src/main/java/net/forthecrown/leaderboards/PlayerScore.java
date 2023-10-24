package net.forthecrown.leaderboards;

import java.util.UUID;
import net.forthecrown.user.Users;
import net.forthecrown.user.name.DisplayIntent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public record PlayerScore(UUID playerId, int value) implements LeaderboardScore {

  @Override
  public @NotNull Component displayName(Audience viewer) {
    return Users.get(playerId).displayName(viewer, DisplayIntent.HOVER_TEXT);
  }
}
