package net.forthecrown.crown;

import net.forthecrown.poshd.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class EventTimer {
    public static final long MILLIS_PER_TICK = 100;

    private long elapsedTime = 0;
    private boolean stopped;

    private final Player player;

    private final TimerMessageFormatter messageFormatter;
    private final Consumer<Player> onTimerExpire;
    public Location checkPoint, exitLocation;

    private final Timer timer;
    private long startTime;

    public EventTimer(Player p, TimerMessageFormatter messageFormatter, Consumer<Player> onTimerExpire) {
        this.player = p;
        this.messageFormatter = messageFormatter;
        this.onTimerExpire = onTimerExpire;

        timer = new Timer();
    }

    public EventTimer(Player p, Consumer<Player> onTimerExpire) {
        this(p, TimerMessageFormatter.defaultTimer(), onTimerExpire);
    }

    public void start(int maxTicks) {
        stopped = false;
        startTime = System.currentTimeMillis();
        long maxMillis = maxTicks * 50L;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime += MILLIS_PER_TICK;
                if(elapsedTime >= maxMillis) {
                    Bukkit.getScheduler().runTask(Main.inst, () -> onTimerExpire.accept(getPlayer()));
                    stop();
                }

                sendActionBar();
            }
        }, 0, MILLIS_PER_TICK);
    }

    public void stop() {
        timer.cancel();
        timer.purge();
        stopped = true;
    }

    public boolean wasStopped() {
        return stopped;
    }

    public long getStartTime() {
        return startTime;
    }

    public Player getPlayer() {
        return player;
    }

    public Component getFormattedMessage() {
        return messageFormatter.format(getTimerCounter(elapsedTime).toString(), elapsedTime);
    }

    private void sendActionBar() {
        player.sendActionBar(getFormattedMessage());
    }

    public static StringBuilder getTimerCounter(final long timeInMillis){
        long minutes = (timeInMillis / 60000) % 60;
        long seconds = (timeInMillis / 1000) % 60;
        long milliseconds = (timeInMillis /10) % 100;

        return new StringBuilder()
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds))
                .append(":")
                .append(String.format("%02d", milliseconds));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventTimer that = (EventTimer) o;
        return player.equals(that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, timer, onTimerExpire);
    }
}
