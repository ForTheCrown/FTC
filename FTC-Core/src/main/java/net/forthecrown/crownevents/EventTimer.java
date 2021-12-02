package net.forthecrown.crownevents;

import net.forthecrown.core.Crown;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class EventTimer {

    private long elapsedTime = 0;
    private boolean stopped;

    private final Player player;

    private final TimerMessageFormatter messageFormatter;
    private final Consumer<Player> onTimerExpire;

    private final Timer timer;

    public EventTimer(Player p, TimerMessageFormatter messageFormatter, Consumer<Player> onTimerExpire){
        this.player = p;
        this.messageFormatter = messageFormatter;
        this.onTimerExpire = onTimerExpire;

        timer = new Timer();
    }

    public EventTimer(Player p, Consumer<Player> onTimerExpire){
        this(p, TimerMessageFormatter.defaultTimer(), onTimerExpire);
    }

    public void start(int maxTicks){
        stopped = false;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime += 100;
                if(elapsedTime >= maxTicks * 50){
                    Bukkit.getScheduler().runTask(Crown.inst(), () -> onTimerExpire.accept(getPlayer()));
                    stop();
                }

                sendActionBar();
            }
        }, 0, 100);
    }

    public void startTickingDown(int ticks){
        elapsedTime = ticks * 50;
        stopped = false;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime -= 100;

                if(elapsedTime <= 0){
                    Bukkit.getScheduler().runTask(Crown.inst(), () -> onTimerExpire.accept(getPlayer()));
                    stop();
                }

                sendActionBar();
            }
        }, 0, 100);
    }

    public void stop(){
        timer.cancel();
        timer.purge();
        stopped = true;
    }

    public boolean wasStopped(){
        return stopped;
    }

    public long getTime(){
        return elapsedTime;
    }

    public void setTime(long millis) {
        this.elapsedTime = millis;
    }

    public long getTimeInTicks() {
        return getTime() / 50;
    }

    public void setTimeInTicks(long time) {
        this.elapsedTime = time * 50;
    }

    public Player getPlayer() {
        return player;
    }

    public Consumer<Player> getOnTimerExpire() {
        return onTimerExpire;
    }

    public TimerMessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    private void sendActionBar(){
        player.sendActionBar(messageFormatter.format(getTimerCounter(elapsedTime).toString(), elapsedTime));
    }

    public static StringBuilder getTimerCounter(final long timeInMillis){
        long minutes = (timeInMillis / 60000) % 60;
        long seconds = (timeInMillis / 1000) % 60;
        long milliseconds = (timeInMillis /10) % 100;

        return new StringBuilder()
                .append(String.format("%02d", minutes)).append(":")
                .append(String.format("%02d", seconds)).append(":")
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
