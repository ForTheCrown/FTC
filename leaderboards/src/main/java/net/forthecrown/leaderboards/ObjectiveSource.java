package net.forthecrown.leaderboards;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import net.forthecrown.user.UserLookup.LookupEntry;
import net.forthecrown.user.Users;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public record ObjectiveSource(String objectiveName) implements LeaderboardSource {

  private Optional<Objective> getObjective() {
    var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    return Optional.ofNullable(scoreboard.getObjective(objectiveName));
  }

  @Override
  public List<LeaderboardScore> getScores() {
    return getObjective()
        .map(objective -> {
          Set<String> entries = objective.getScoreboard().getEntries();
          List<LeaderboardScore> scores = new ArrayList<>();

          for (String entry : entries) {
            Score score = objective.getScore(entry);

            if (!score.isScoreSet()) {
              continue;
            }

            LookupEntry lookupEntry = Users.getService().getLookup().query(entry);

            if (lookupEntry == null) {
              scores.add(new ArbitraryScore(entry, score.getScore()));
            } else {
              scores.add(new PlayerScore(lookupEntry.getUniqueId(), score.getScore()));
            }
          }

          return scores;
        })
        .orElseGet(List::of);
  }

  @Override
  public OptionalInt getScore(UUID playerId) {
    return getObjective()
        .flatMap(objective -> {
          OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);

          if (Strings.isNullOrEmpty(player.getName())) {
            return Optional.empty();
          }

          Score score = objective.getScore(player);

          if (!score.isScoreSet()) {
            return Optional.empty();
          }

          return Optional.of(score.getScore());
        })
        .map(OptionalInt::of)
        .orElseGet(OptionalInt::empty);
  }
}
