package net.forthecrown.leaderboards;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import net.forthecrown.utils.ScoreIntMap;

public record ScoreMapSource(ScoreIntMap<UUID> map, float divider) implements LeaderboardSource {

  public ScoreMapSource(ScoreIntMap<UUID> map) {
    this(map, 1f);
  }

  @Override
  public List<LeaderboardScore> getScores() {
    List<LeaderboardScore> scores = new ArrayList<>(map.size());
    map.forEach(uuidEntry -> {
      scores.add(new PlayerScore(uuidEntry.key(), (int) (uuidEntry.value() / divider)));
    });
    return scores;
  }

  @Override
  public OptionalInt getScore(UUID playerId) {
    if (!map.contains(playerId)) {
      return OptionalInt.empty();
    }
    return OptionalInt.of((int) (map.get(playerId) / divider));
  }
}
