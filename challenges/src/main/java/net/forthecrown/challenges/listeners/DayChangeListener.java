package net.forthecrown.challenges.listeners;

import java.time.DayOfWeek;
import net.forthecrown.challenges.ChallengeManager;
import net.forthecrown.challenges.ItemChallenge;
import net.forthecrown.challenges.ResetInterval;
import net.forthecrown.events.DayChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

class DayChangeListener implements Listener {

  private final ChallengeManager manager;

  public DayChangeListener(ChallengeManager manager) {
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true)
  public void onDayChange(DayChangeEvent event) {
    var time = event.getTime();
    var date = time.toLocalDate();

    var registry = manager.getChallengeRegistry();
    var storage = manager.getStorage();

    manager.setDate(date);

    if (time.getDayOfWeek() == DayOfWeek.MONDAY) {
      // Clear all item challenge's used items
      // list, so they can be selected again
      for (var h : registry.entries()) {
        if (!(h.getValue() instanceof ItemChallenge)) {
          continue;
        }

        var container = storage.loadContainer(h);

        if (container.getUsed().isEmpty()) {
          continue;
        }

        container.getUsed().clear();
        storage.saveContainer(container);
      }

      manager.reset(ResetInterval.WEEKLY);
    }

    manager.reset(ResetInterval.DAILY);
  }
}
