package net.forthecrown.guilds.leaderboards;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.forthecrown.guilds.Guild;
import net.forthecrown.guilds.GuildManager;
import net.forthecrown.leaderboards.LeaderboardScore;
import net.forthecrown.leaderboards.LeaderboardSource;

@RequiredArgsConstructor
public class GuildLeaderboardSource implements LeaderboardSource {

  private final GuildManager manager;
  private final ScoreAccessor accessor;

  @Override
  public List<LeaderboardScore> getScores() {
    var guilds = manager.getGuilds();
    List<LeaderboardScore> scores = new ArrayList<>(guilds.size());

    for (Guild guild : guilds) {
      int score = accessor.getScore(guild, manager);
      scores.add(new GuildLeaderboardScore(guild, score));
    }

    return scores;
  }

  @Override
  public OptionalInt getScore(UUID playerId) {
    return OptionalInt.empty();
  }

  public enum ScoreAccessor {
    MEMBERS {
      @Override
      int getScore(Guild guild, GuildManager manager) {
        return guild.getMemberSize();
      }
    },

    CHUNKS {
      @Override
      int getScore(Guild guild, GuildManager manager) {
        return manager.getGuildChunkAmount(guild);
      }
    },

    GUILD_EXP {
      @Override
      int getScore(Guild guild, GuildManager manager) {
        return (int) guild.getTotalExp();
      }
    };

    abstract int getScore(Guild guild, GuildManager manager);
  }
}
