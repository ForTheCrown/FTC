package net.forthecrown.core.crownevents;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.utils.ComponentUtils;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;
import net.minecraft.server.v1_16_R3.SystemUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class EventTimer {

    private long elapsedTime = 0;
    private final Player player;
    private final Timer timer;
    private final Consumer<Player> onTimerExpire;
    private boolean stopped;

    public EventTimer(Player p, Consumer<Player> onTimerExpire){
        player = p;
        this.onTimerExpire = onTimerExpire;
        timer = new Timer();
    }

    public void startTimer(int maxMinutes){
        stopped = false;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime += 100;

                int minutes = (int) ((elapsedTime /60000) % 60);
                if(minutes >= maxMinutes){
                    Bukkit.getScheduler().runTask(FtcCore.getInstance(), () -> onTimerExpire.accept(getPlayer()));
                    stopTimer();
                }

                sendActionBar(player, getTimerCounter(elapsedTime).toString());
            }
        }, 0, 100);
    }

    public void startTimerTickingDown(int maxTimeMins){
        elapsedTime = maxTimeMins;
        stopped = false;

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime -= 100;

                if(elapsedTime <= 0){
                    Bukkit.getScheduler().runTask(FtcCore.getInstance(), () -> onTimerExpire.accept(getPlayer()));
                    stopTimer();
                }

                sendActionBar(player, getTimerCounter(elapsedTime).toString());
            }
        }, 0, 100);
    }

    public void stopTimer(){
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

    private static void sendActionBar(Player p, String message){
        CraftPlayer c = (CraftPlayer) p;
        IChatBaseComponent text = ComponentUtils.stringToVanilla(message);
        PacketPlayOutChat packet = new PacketPlayOutChat(text, ChatMessageType.GAME_INFO, SystemUtils.b);
        c.getHandle().playerConnection.sendPacket(packet);
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
