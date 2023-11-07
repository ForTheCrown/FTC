package net.forthecrown.leaderboards.commands;

import static net.forthecrown.leaderboards.LeaderboardSources.OBJECTIVE_PREFIX;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.forthecrown.command.arguments.Arguments;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.leaderboards.LeaderboardSource;
import net.forthecrown.leaderboards.LeaderboardSources;
import net.forthecrown.leaderboards.ServiceImpl;
import net.forthecrown.registry.Holder;
import net.forthecrown.registry.Registry;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class SourceArgument implements ArgumentType<Holder<LeaderboardSource>> {

  private final Registry<LeaderboardSource> sources;

  public SourceArgument(ServiceImpl service) {
    this.sources = service.getSources();
  }

  static Scoreboard scoreboard() {
    return Bukkit.getScoreboardManager().getMainScoreboard();
  }

  @Override
  public Holder<LeaderboardSource> parse(StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();
    String ftcKey = Arguments.FTC_KEY.parse(reader);

    DataResult<Holder<LeaderboardSource>> dataResult = LeaderboardSources.get(ftcKey);

    if (dataResult.error().isPresent()) {
      reader.setCursor(start);
      var err = dataResult.error().get().message();
      throw Grenadier.exceptions().createWithContext(err, reader);
    }

    return dataResult.result().get();
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(
      CommandContext<S> context,
      SuggestionsBuilder builder
  ) {
    Set<String> keys = new HashSet<>(sources.keys());

    scoreboard().getObjectives()
        .stream()
        .map(Objective::getName)
        .map(name -> OBJECTIVE_PREFIX + name)
        .forEach(keys::add);

    return Completions.suggest(builder, keys);
  }
}
