package net.forthecrown.leaderboards;

import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

public enum DummySource implements LeaderboardSource {
  INSTANCE;

  @Override
  public List<LeaderboardScore> getScores() {
    return List.of();
  }

  @Override
  public OptionalInt getScore(UUID playerId) {
    return OptionalInt.empty();
  }
}
