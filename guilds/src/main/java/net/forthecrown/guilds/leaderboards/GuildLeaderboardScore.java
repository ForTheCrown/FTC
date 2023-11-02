package net.forthecrown.guilds.leaderboards;

import java.util.UUID;
import net.forthecrown.guilds.Guild;
import net.forthecrown.leaderboards.LeaderboardScore;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record GuildLeaderboardScore(Guild guild, int value) implements LeaderboardScore {

  @Override
  public @Nullable UUID playerId() {
    return null;
  }

  @Override
  public @NotNull Component displayName(Audience viewer) {
    return guild.getSettings().getNameFormat().apply(guild);
  }
}
