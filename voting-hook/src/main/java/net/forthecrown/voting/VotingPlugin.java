package net.forthecrown.voting;

import com.bencodez.votingplugin.topvoter.TopVoter;
import net.forthecrown.events.Events;
import net.forthecrown.leaderboards.LeaderboardSource;
import net.forthecrown.leaderboards.Leaderboards;
import net.forthecrown.registry.Registry;
import org.bukkit.plugin.java.JavaPlugin;

public class VotingPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    Events.register(new VoteListener());

    Registry<LeaderboardSource> sources = Leaderboards.getSources();
    sources.register("votes/daily",   new VoteLeaderboardSource(TopVoter.Daily));
    sources.register("votes/monthly", new VoteLeaderboardSource(TopVoter.Monthly));
    sources.register("votes/weekly",  new VoteLeaderboardSource(TopVoter.Weekly));
    sources.register("votes/alltime", new VoteLeaderboardSource(TopVoter.AllTime));
  }

  @Override
  public void onDisable() {

  }
}
