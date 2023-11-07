package net.forthecrown.voting;

import com.bencodez.votingplugin.VotingPluginMain;
import com.bencodez.votingplugin.topvoter.TopVoter;
import com.bencodez.votingplugin.topvoter.TopVoterPlayer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.UUID;
import net.forthecrown.Loggers;
import net.forthecrown.leaderboards.LeaderboardScore;
import net.forthecrown.leaderboards.LeaderboardSource;
import net.forthecrown.leaderboards.PlayerScore;
import net.forthecrown.user.UserLookup;
import net.forthecrown.user.UserService;
import net.forthecrown.user.Users;
import org.slf4j.Logger;

public class VoteLeaderboardSource implements LeaderboardSource {

  private static final Logger LOGGER = Loggers.getLogger();

  private final TopVoter category;

  public VoteLeaderboardSource(TopVoter category) {
    this.category = category;
  }

  @Override
  public List<LeaderboardScore> getScores() {
    var main = VotingPluginMain.getPlugin();
    LinkedHashMap<TopVoterPlayer, Integer> map = main.getTopVoter(category);

    UserService service = Users.getService();
    UserLookup lookup = service.getLookup();

    List<LeaderboardScore> scores = new ArrayList<>(map.size());
    map.forEach((topVoterPlayer, integer) -> {
      var lookupEntry = lookup.getEntry(topVoterPlayer.getUuid());

      if (lookupEntry == null) {
        // :shrug: IDK, there are invalid players' UUIDs in this list
        //LOGGER.warn("Failed to find user entry for UUID {}, votes={}",
        //    topVoterPlayer.getUuid(), integer
        //);
        return;
      }

      scores.add(new PlayerScore(topVoterPlayer.getUuid(), integer));
    });

    return scores;
  }

  @Override
  public OptionalInt getScore(UUID playerId) {
    var main = VotingPluginMain.getPlugin();
    LinkedHashMap<TopVoterPlayer, Integer> map = main.getTopVoter(category);

    for (Entry<TopVoterPlayer, Integer> entry : map.entrySet()) {
      if (entry.getKey().getUuid().equals(playerId)) {
        return OptionalInt.of(entry.getValue());
      }
    }

    return OptionalInt.empty();
  }
}
