package ftc.crownapi.types;

import ftc.crownapi.Main;
import ftc.crownapi.types.interfaces.CrownEventIUser;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CrownEventUser implements CrownEventIUser {

    private static final CrownEvent crownMain = new CrownEvent();

    //server Locations
    private static final Location hazLoc = new Location(Bukkit.getWorld("world"), 200.5, 70, 1000.5);
    private static final Location questLoc = new Location(Bukkit.getWorld("world"), -599.5, 66, 3800.5);
    private static final Location ketilLoc = new Location(Bukkit.getWorld("world"), -1800.5, 82, -2199.5);

    //timer variables
    private static final Timer timer = new Timer();
    private static long timerTime;
    private static long timeLeft;

    //Base player variable and Constructor
    private final Player base;
    public CrownEventUser(Player base){
        this.base = base;
    }

    //the void of interface methods lol
    @Override
    public boolean isInEvent() {
        return crownMain.getPlayersInEvent().contains(base);
    }
    @Override
    public void setInEvent(boolean value) {
        List<Player> list = crownMain.getPlayersInEvent();
        if(value){
            list.add(base);
            crownMain.setPlayersInEvent(list);
            return;
        }
        if(isInEvent()){
            list.remove(base);
            crownMain.setPlayersInEvent(list);
        }
    }

    @Override
    public void teleportToHazelguard() {
        base.teleport(hazLoc);
    }
    @Override
    public void teleportToQuestmoor() {
        base.teleport(questLoc);
    }
    @Override
    public void teleportToKetilheim() {
        base.teleport(ketilLoc);
    }

    @Override
    public void disqualify() {
        Main.plugin.disqualifiedPlayersList.add(base);
        if(isInEvent()) teleportToHazelguard();
    }
    @Override
    public boolean isDisqualified(){
        return crownMain.getDisqualifiedPlayers().contains(base);
    }


    @Override
    public int getScoreMapScore() {
        return Main.plugin.scoreMap.getOrDefault(base, 0);
    }
    @Override
    public void setScoreMapScore(int score) {
        Map<Player, Integer> score_map = crownMain.getScoreMap();
        score_map.put(base, score);
        crownMain.setScoreMap(score_map);
    }

    @Override
    public void addToScoreMap() {
        Map<Player, Integer> score_map = crownMain.getScoreMap();
        score_map.put(base, 0);
        crownMain.setScoreMap(score_map);
    }

    @Override
    public void removeFromScoreMap() {
        Map<Player, Integer> score_map = crownMain.getScoreMap();
        score_map.remove(base);
        crownMain.setScoreMap(score_map);
    }

    @Override
    public Score getCrownScore() {
        return crownMain.getCrownObjective().getScore(base.getName());
    }
    @Override
    public int scoreMapCrownScoreDifference() {
        return getScoreMapScore() - getCrownScore().getScore();
    }


    @Override
    public void teleportToEventLobby() {
        base.teleport(crownMain.getLobbyLocation());
    }
    @Override
    public void teleportToEventStart() {
        base.teleport(crownMain.getStartLocation());
    }


    @Override
    public void setBalance(int amount) {
        //I don't fuggin know lol
    }
    @Override
    public int getBalance() {
        // :shrug:
        return 0;
    }


    @Override
    public void setGems(int amount) {
    }
    @Override
    public int getGems() {
        return 0;
    }

    @Override
    public boolean hasQuitInEvent() {
        return crownMain.getPlayersThatQuitInEvent().contains(base);
    }

    @Override
    public void setHasQuitInEvent(boolean value) {
        if(value){
            List<Player> plrList = crownMain.getPlayersThatQuitInEvent();
            plrList.add(base);
            crownMain.setPlayersThatQuitInEventList(plrList);
            return;
        }
        if(hasQuitInEvent()){
            List<Player> plrList = crownMain.getPlayersThatQuitInEvent();
            plrList.remove(base);
            crownMain.setPlayersThatQuitInEventList(plrList);
        }
    }


    @Override
    public CrownEventUser getUser() {
        return this;
    }

    @Override
    public Player getPlayer() {
        return base;
    }

    @Override
    public void startTimer() {
        timerTime = 0;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerTime += 100;
                long minutes = (timerTime /60000) % 60;
                long seconds = (timerTime / 1000) % 60;
                long milliseconds = (timerTime/100 ) % 100;

                StringBuilder message = new StringBuilder("Timer: ");
                message.append(String.format("%02d", minutes)).append(":");
                message.append(String.format("%02d", seconds)).append(":");
                message.append(String.format("%02d", milliseconds));

                base.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message.toString()));
            }
        }, 0, 100);
    }
    @Override
    public void stopTimer() {
        timer.cancel();
    }
    @Override
    public void purgeTimer() {
        timer.purge();
    }
    @Override
    public long getTimerEndTime() {
        return timerTime;
    }

    @Override
    public void startTimerTickingDown(long timeInSeconds) { //What the fuck is this
        timeLeft = timeInSeconds * 1000;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(timeLeft <= 0 ) timer.cancel();
                timeLeft -= 100;
                long minutes = (timeLeft /60000) % 60;
                long seconds = (timeLeft / 1000) % 60;
                long milliseconds = (timeLeft/100 ) % 100;

                StringBuilder message = new StringBuilder("Timer: ");
                message.append(String.format("%02d", minutes)).append(":");
                message.append(String.format("%02d", seconds)).append(":");
                message.append(String.format("%02d", milliseconds));

                base.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message.toString()));
            }
        }, 0, 100);
    }
    @Override
    public long getTimerTickingDownTimeLeft() {
        return timeLeft;
    }
}