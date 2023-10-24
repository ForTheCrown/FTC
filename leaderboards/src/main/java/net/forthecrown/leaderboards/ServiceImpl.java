package net.forthecrown.leaderboards;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import net.forthecrown.registry.Registries;
import net.forthecrown.registry.Registry;
import net.forthecrown.utils.Result;

@Getter
public class ServiceImpl implements LeaderboardService {

  private final Registry<LeaderboardSource> sources = Registries.newRegistry();
  private final Map<String, BoardImpl> boards = new Object2ObjectOpenHashMap<>();
  private final Int2ObjectMap<BoardData> byEntityId = new Int2ObjectOpenHashMap<>();

  @Override
  public Optional<Leaderboard> getLeaderboard(String name) {
    return Optional.ofNullable(boards.get(name));
  }

  // Returns non-API version of leaderboard
  public Optional<BoardImpl> getBoard(String name) {
    return Optional.ofNullable(boards.get(name));
  }

  @Override
  public Result<Leaderboard> createLeaderboard(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return Result.error("Null/blank name");
    }
    if (!Registries.isValidKey(name)) {
      return Result.error("Invalid key, must match pattern %s", Registries.VALID_KEY_REGEX);
    }
    if (boards.containsKey(name)) {
      return Result.error("Name already in use");
    }

    BoardImpl board = new BoardImpl(name);
    boards.put(name, board);

    return Result.success(board);
  }

  @Override
  public Set<String> getExistingLeaderboards() {
    return Collections.unmodifiableSet(boards.keySet());
  }
}
