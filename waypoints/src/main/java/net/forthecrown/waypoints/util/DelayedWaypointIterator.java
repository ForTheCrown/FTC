package net.forthecrown.waypoints.util;

import java.util.Iterator;
import net.forthecrown.utils.Tasks;
import net.forthecrown.waypoints.Waypoint;
import org.bukkit.scheduler.BukkitTask;

public class DelayedWaypointIterator implements Runnable {

  static final int ACTIONS_PER_TICK = 5;

  private final Iterator<Waypoint> base;
  private final WaypointAction action;

  private BukkitTask task;

  public DelayedWaypointIterator(
      Iterator<Waypoint> base,
      WaypointAction action
  ) {
    this.base = base;
    this.action = action;
  }

  public void schedule() {
    Tasks.cancel(task);
    this.task = Tasks.runTimer(this, 1, 1);
  }

  @Override
  public void run() {
    int executed = 0;

    try {
      while (executed < ACTIONS_PER_TICK && base.hasNext()) {
        var n = base.next();
        action.accept(n);
        executed++;
      }
    } finally {
      if (!base.hasNext()) {
        Tasks.cancel(task);
        action.onFinish();
      }
    }
  }
}
