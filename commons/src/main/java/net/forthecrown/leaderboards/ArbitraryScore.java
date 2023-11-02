package net.forthecrown.leaderboards;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ArbitraryScore(String name, int value) implements LeaderboardScore {

  @Override
  public @Nullable UUID playerId() {
    return null;
  }

  @Override
  public @NotNull Component displayName(Audience viewer) {
    return Component.text(name);
  }
}
