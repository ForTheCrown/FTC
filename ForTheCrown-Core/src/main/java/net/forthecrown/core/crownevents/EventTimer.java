package net.forthecrown.core.crownevents;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class EventTimer {

    private long elapseTime = 0;
    private final Player player;
    private final Timer timer;
    private final Runnable runnable;

    public EventTimer(Player p, Runnable onTimerExpire){
        player = p;
        runnable = onTimerExpire;
        timer = new Timer();
    }

    public void startTimer(int maxMinutes){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapseTime += 100;

                int minutes = (int) ((elapseTime /60000) % 60);
                if(minutes >= maxMinutes){
                    runnable.run();
                    stopTimer();
                    return;
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(getTimerCounter(elapseTime).toString()));
            }
        }, 0, 100);
    }

    public void startTimerTickingDown(int maxTimeMins){
        elapseTime = maxTimeMins;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapseTime -= 100;

                if(elapseTime <= 0){
                    runnable.run();
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(getTimerCounter(elapseTime).toString()));
            }
        }, 0, 100);
    }

    public void stopTimer(){
        timer.cancel();
        timer.purge();
    }

    public long getPlayerTime(){
        return elapseTime;
    }

    public Player getPlayer() {
        return player;
    }



    public static StringBuilder getTimerCounter(long timeInMillis){
        long minutes = (timeInMillis /60000) % 60;
        long seconds = (timeInMillis / 1000) % 60;
        long milliseconds = (timeInMillis/100 ) % 100;

        StringBuilder message = new StringBuilder();
        message.append(String.format("%02d", minutes)).append(":");
        message.append(String.format("%02d", seconds)).append(":");
        message.append(String.format("%02d", milliseconds));

        return message;
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
        return Objects.hash(player);
    }
}
