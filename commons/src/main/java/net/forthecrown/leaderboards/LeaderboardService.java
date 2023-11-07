package net.forthecrown.leaderboards;

import java.util.Optional;
import java.util.Set;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.Result;

public interface LeaderboardService {

  Registry<LeaderboardSource> getSources();

  Optional<Leaderboard> getLeaderboard(String name);

  Result<Leaderboard> createLeaderboard(String name);

  boolean removeLeaderboard(String name);

  Set<String> getExistingLeaderboards();

  void updateWithSource(Holder<LeaderboardSource> source);
}
