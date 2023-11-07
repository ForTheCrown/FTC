package net.forthecrown.core.user;

import static net.kyori.adventure.text.Component.text;

import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scheduler.BukkitTask;

/**
 * A small class that ensures vanished users are told they are in vanish all the time
 */
public class UserVanishTicker implements Runnable {

  /**
   * The interval at which staff are reminded they are in vanish
   */
  public static final int TICK_INTERVAL = 2 * 20;
  private static final Component YOU_ARE_IN_VANISH = text("YOU ARE IN VANISH", NamedTextColor.RED);

  private final BukkitTask task;
  private final User user;

  UserVanishTicker(User user) {
    this.user = user;
    this.task = Tasks.runTimer(this, TICK_INTERVAL, TICK_INTERVAL);
  }

  void stop() {
    task.cancel();
  }

  @Override
  public void run() {
    user.sendActionBar(YOU_ARE_IN_VANISH);
  }
}