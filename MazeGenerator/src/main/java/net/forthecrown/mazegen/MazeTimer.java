package net.forthecrown.mazegen;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MazeTimer {

    public static Map<Player, MazeTimer> playersWithTimer = new HashMap<>();
    private Timer timer;
    private Player player;

    public MazeTimer(Player player){
        this.player = player;
        playersWithTimer.put(player, this);
    }

    public void startTimer(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

            }
        }, 0, 100);
    }

    public void stopTimer(){
        timer.cancel();
        timer.purge();
    }
}
