package net.forthecrown.leaderboards;

import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;

public interface LeaderboardSource {

  List<LeaderboardScore> getScores();

  OptionalInt getScore(UUID playerId);
}
