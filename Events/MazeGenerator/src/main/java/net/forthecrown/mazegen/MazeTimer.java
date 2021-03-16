package net.forthecrown.mazegen;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Timer;
import java.util.TimerTask;

public class MazeTimer {

    private final Main main;
    public MazeTimer(Main main){
        this.main = main;
    }

    private Timer timer;
    private Player player;
    private int elapsedTime;

    public void startTimer(Player player){
        timer = new Timer();
        elapsedTime = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                elapsedTime += 100;
                int minutes = (elapsedTime /60000) % 60;
                int seconds = (elapsedTime / 1000) % 60;
                int milliseconds = (elapsedTime/100 ) % 100;

                if(minutes >= 5){
                    Bukkit.getScheduler().runTask(main, () -> main.endEvent(player));
                    player.sendMessage(ChatColor.GRAY + "You took too long!");
                    return;
                }

                StringBuilder message = new StringBuilder("Timer: ");
                message.append(String.format("%02d", minutes)).append(":");
                message.append(String.format("%02d", seconds)).append(":");
                message.append(String.format("%02d", milliseconds));

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message.toString()));
            }
        }, 0, 100);
    }

    public void destroyTimer(){
        timer.cancel();
        timer.purge();
    }

    public StringBuilder getTimerMessage(int elapsedTime){
        int minutes = (elapsedTime /60000) % 60;
        int seconds = (elapsedTime / 1000) % 60;
        int milliseconds = (elapsedTime/100 ) % 100;

        StringBuilder message = new StringBuilder();
        message.append(String.format("%02d", minutes)).append(":");
        message.append(String.format("%02d", seconds)).append(":");
        message.append(String.format("%02d", milliseconds));

        return message;
    }

    public int getPlayerTime(){
        return elapsedTime;
    }
}
