package net.forthecrown.user;

import net.forthecrown.utils.Tasks;
import org.bukkit.scheduler.BukkitTask;

import static net.forthecrown.core.Messages.YOU_ARE_IN_VANISH;

/**
 * A small class that ensures vanished users are told they are in vanish all the time
 */
public class UserVanishTicker implements Runnable {
    /**
     * The interval at which staff are reminded they are in vanish
     */
    public static final int TICK_INTERVAL = 2 * 20;

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