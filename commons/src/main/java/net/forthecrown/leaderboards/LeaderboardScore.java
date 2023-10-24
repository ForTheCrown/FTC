package net.forthecrown.leaderboards;

import java.util.UUID;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LeaderboardScore {

  @Nullable
  UUID playerId();

  @NotNull
  Component displayName(Audience viewer);

  int value();
}
