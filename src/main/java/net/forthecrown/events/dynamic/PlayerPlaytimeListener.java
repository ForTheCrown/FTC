package net.forthecrown.events.dynamic;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.forthecrown.core.challenge.ChallengeHandle;
import net.forthecrown.utils.Tasks;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter
@RequiredArgsConstructor
public class PlayerPlaytimeListener {
  private final ChallengeHandle handle;
  private final long tickInterval;

  private final Map<UUID, BukkitTask> taskMap = new HashMap<>();

  public void startTask(Player player) {
    stopTask(player);

    taskMap.put(player.getUniqueId(), Tasks.runTimer(() -> {
      handle.givePoint(player);

      if (handle.hasCompleted(player)) {
        stopTask(player);
      }
    }, tickInterval, tickInterval));
  }

  public void stopTask(Player player) {
    var existing = taskMap.get(player.getUniqueId());

    if (existing == null) {
      return;
    }

    Tasks.cancel(existing);
  }

  public void clear() {
    taskMap.values().forEach(Tasks::cancel);
    taskMap.clear();
  }
}