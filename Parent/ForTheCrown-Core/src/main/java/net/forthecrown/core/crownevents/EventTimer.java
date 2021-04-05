package net.forthecrown.core.crownevents;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserManager;
import net.forthecrown.core.utils.ComponentUtils;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class EventTimer {

    private long elapsedTime = 0;
    private boolean stopped;

    private final Player player;
    private final String format;
    private final CrownUser user;
    private final Timer timer;
    private final Consumer<Player> onTimerExpire;

    public EventTimer(Player p, String format, Consumer<Player> onTimerExpire){
        this.player = p;
        this.user = UserManager.getUser(p);
        this.format = format;
        this.onTimerExpire = onTimerExpire;

        timer = new Timer();
    }

    public EventTimer(Player p, Consumer<Player> onTimerExpire){
        this(p, "%s", onTimerExpire);
    }

    public void start(int maxMinutes){
        stopped = false;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime += 100;
                if(elapsedTime >= maxMinutes*1000*60){
                    Bukkit.getScheduler().runTask(FtcCore.getInstance(), () -> onTimerExpire.accept(getPlayer()));
                    stop();
                }

                sendActionBar(getTimerCounter(elapsedTime).toString());
            }
        }, 0, 100);
    }

    public void startTickingDown(int maxTimeMins){
        elapsedTime = maxTimeMins*60*1000;
        stopped = false;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime -= 100;

                if(elapsedTime <= 0){
                    Bukkit.getScheduler().runTask(FtcCore.getInstance(), () -> onTimerExpire.accept(getPlayer()));
                    stop();
                }

                sendActionBar(getTimerCounter(elapsedTime).toString());
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

    public Player getPlayer() {
        return player;
    }

    public static StringBuilder getTimerCounter(final long timeInMillis){
        long minutes = (timeInMillis / 60000) % 60;
        long seconds = (timeInMillis / 1000) % 60;
        long milliseconds = (timeInMillis /10) % 100;

        final StringBuilder message = new StringBuilder()
                .append(String.format("%02d", minutes)).append(":")
                .append(String.format("%02d", seconds)).append(":")
                .append(String.format("%02d", milliseconds));

        return message;
    }

    private void sendActionBar(String message){
        user.sendMessage(ComponentUtils.stringToVanilla(String.format(format, message)), ChatMessageType.GAME_INFO);
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
