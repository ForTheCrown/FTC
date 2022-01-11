package net.forthecrown.user;

import net.forthecrown.core.Crown;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class UserVanishTicker implements Runnable {
    public static final int TICK_INTERVAL = 3 * 20;
    public static final Component TEXT = Component.text("YOU ARE IN VANISH").color(NamedTextColor.WHITE);

    private final BukkitTask task;
    private final FtcUser user;

    UserVanishTicker(FtcUser user) {
        this.user = user;
        this.task = Bukkit.getScheduler().runTaskTimer(Crown.inst(), this, TICK_INTERVAL, TICK_INTERVAL);
    }

    void stop() {
        task.cancel();
    }

    @Override
    public void run() {
        user.sendActionBar(TEXT);
    }
}
