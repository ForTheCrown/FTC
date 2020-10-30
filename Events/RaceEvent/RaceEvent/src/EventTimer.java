


import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class EventTimer {

    private int elapsedTime;
    private Timer timer = new Timer();

    public void startTimer(Player player){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                elapsedTime += 100;
                int minutes = (elapsedTime /60000) % 60;
                int seconds = (elapsedTime / 1000) % 60;
                int milliseconds = (elapsedTime/100 ) % 100;

                StringBuilder message = new StringBuilder("Timer: ");
                message.append(String.format("%02d", minutes)).append(":");
                message.append(String.format("%02d", seconds)).append(":");
                message.append(String.format("%02d", milliseconds));

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message.toString()));
            }
        }, 0, 100);
    }


}
