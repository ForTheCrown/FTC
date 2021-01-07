package ftc.crownapi.types;

import ftc.crownapi.EventApi;
import ftc.crownapi.apievents.CrownLeaveEvent;
import ftc.crownapi.types.interfaces.CrownEventIUser;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Score;

import java.util.*;

public class CrownEventUser implements CrownEventIUser {

    private final CrownEvent crownMain;
    private final EventApi main;

    //server Locations
    private static final Location hazLoc = new Location(Bukkit.getWorld("world"), 200.5, 70, 1000.5);
    private static final Location questLoc = new Location(Bukkit.getWorld("world"), -599.5, 66, 3800.5);
    private static final Location ketilLoc = new Location(Bukkit.getWorld("world"), -1800.5, 82, -2199.5);

    //timer variables
    private Timer timer;
    private long timerTime;
    private long timeLeft;
    private boolean hasTimer = false;

    //Base player variable and Constructor
    private final Player base;
    public CrownEventUser(Player base, EventApi main, CrownEvent crownMain){
        this.base = base;
        this.main = main;
        this.crownMain = crownMain;
        EventApi.loadedUsers.add(this);
    }

    //the void of interface methods lol
    @Override
    public boolean isInEvent() {
        return crownMain.getPlayersInEvent().contains(base.getUniqueId());
    }
    @Override
    public void setInEvent(boolean value) {
        List<UUID> list = crownMain.getPlayersInEvent();
        if(value){
            list.add(base.getUniqueId());
            crownMain.setPlayersInEvent(list);
            return;
        }
        if(isInEvent()){
            list.remove(base.getUniqueId());
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
    public void setDisqualified(boolean value) {
        List<UUID> list = crownMain.getDisqualifiedPlayers();
        if(!value && list.contains(base.getUniqueId())){
            list.remove(base.getUniqueId());
            crownMain.setDisqualifiedPlayers(list);
            return;
        }
        if (value) {
            list.add(base.getUniqueId());
            crownMain.setDisqualifiedPlayers(list);
            if (isInEvent()) teleportToHazelguard();
            getCrownScore().setScore(getCrownScore().getScore() * -1);
        }
    }
    @Override
    public boolean isDisqualified(){
        return crownMain.getDisqualifiedPlayers().contains(base.getUniqueId());
    }


    @Override
    public int getScoreMapScore() {
        return main.scoreMap.getOrDefault(base, 0);
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
    public boolean isRecordSmallerThanScore(){
        return getCrownScore().getScore() < getScoreMapScore();
    }


    @Override
    public void teleportToEventLobby() {
        if(crownMain.getLobbyLocation() != null) base.teleport(crownMain.getLobbyLocation());
    }
    @Override
    public void teleportToEventStart() {
        if(crownMain.getStartLocation() != null) base.teleport(crownMain.getStartLocation());
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
        return crownMain.getPlayersThatQuitInEvent().contains(base.getUniqueId());
    }

    @Override
    public void setHasQuitInEvent(boolean value) {
        List<UUID> plrList = crownMain.getPlayersThatQuitInEvent();
        if(value){
            plrList.add(base.getUniqueId());
            crownMain.setPlayersThatQuitInEventList(plrList);
            return;
        }
        if(hasQuitInEvent()){
            plrList.remove(base.getUniqueId());
        }
        crownMain.setPlayersThatQuitInEventList(plrList);
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
    public boolean hasTimer(){
        return hasTimer;
    }
    @Override
    public void startTimer() {
        timer = new Timer();
        timerTime = 0;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!hasTimer) hasTimer = true;
                timerTime += 100;
                if(timerTime > 3540000) stopTimer();

                base.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(crownMain.getTimerString(timerTime).toString()));
            }
        }, 0, 100);
    }
    @Override
    public void stopTimer() {
        timer.cancel();
        timer.purge();
        hasTimer = false;
    }
    @Override
    public long getTimerEndTime() {
        return timerTime;
    }

    @Override
    public void startTimerTickingDown(long timeInSeconds) { //What the fuck is this
        timer = new Timer();
        timeLeft = timeInSeconds * 1000;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(!hasTimer) hasTimer = true;

                //if timer has run out
                if(timeLeft <= 0 ) {
                    stopTimer();
                    //To quote Wout: "Async to sync >:|"
                    Bukkit.getServer().getScheduler().runTask(main, () -> Bukkit.getPluginManager().callEvent(new CrownLeaveEvent(getUser(), getPlayer().getLocation(), getTimerTickingDownTimeLeft(), true)));
                    return;
                }
                timeLeft -= 100;

                base.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(crownMain.getTimerString(timeLeft).toString()));
            }
        }, 0, 100);
    }
    @Override
    public long getTimerTickingDownTimeLeft() {
        return timeLeft;
    }
}