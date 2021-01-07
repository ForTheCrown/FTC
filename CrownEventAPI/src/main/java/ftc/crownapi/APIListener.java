package ftc.crownapi;

import ftc.crownapi.apievents.CrownEnterEvent;
import ftc.crownapi.apievents.CrownLeaveEvent;
import ftc.crownapi.config.CrownBooleanSettings;
import ftc.crownapi.config.CrownMessages;
import ftc.crownapi.types.CrownEvent;
import ftc.crownapi.types.CrownEventUser;
import ftc.crownapi.types.interfaces.CrownEventIUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Score;

public class APIListener implements Listener {

    private final CrownEvent crownEvent;
    private final EventApi main;

    public APIListener(CrownEvent crownEvent, EventApi main){
        this.crownEvent = crownEvent;
        this.main = main;
    }

    //TP's a player out of the event if they've left while in the event
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        CrownEventUser user = main.getApiUser(player);
        if(!user.hasQuitInEvent()) return;

        if(crownEvent.getSetting(CrownBooleanSettings.TO_SPAWN_ON_END)) user.teleportToHazelguard();
        else user.teleportToEventLobby();
        user.setHasQuitInEvent(false);
    }

    // Adds player to the LeftInEvent players list when they leave
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        CrownEventUser user = main.getApiUser(player);

        if(!user.isInEvent()) return;
        user.setHasQuitInEvent(true);
        user.setInEvent(false);

        if(crownEvent.getSetting(CrownBooleanSettings.CUMULATIVE_POINTS)) user.removeFromScoreMap();
        if(user.hasTimer()) user.stopTimer();
        user = null;
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity();
        CrownEventIUser user = main.getApiUser(player);
        if(!user.isInEvent()) return;

        event.setKeepInventory(true);
        event.setKeepLevel(true);

        Bukkit.getServer().getPluginManager().callEvent(new CrownLeaveEvent(user.getUser(), player.getLocation(), user.getCrownScore().getScore(), user.getScoreMapScore()));
    }

    // Keep in mind: These custom events have to be called on in the event plugins themselves. They don't fire whenever lol

    // Some generic stuffs for when a player enters an event
    @EventHandler
    public void onEventEnter(CrownEnterEvent event){
        if(event.isCancelled()) return;

        final boolean hasTimer = event.getHasTimer();
        final boolean hasTimerCD = event.getHasTimerCountingDown();
        final int timeInSeconds = event.getTimeInSeconds();
        CrownEventUser user = event.getUser();

        //if entree is disqualified
        if(user.isDisqualified()){
            event.setCancelled(true);
            return;
        }
        //timer stuffs
        if(hasTimer) user.startTimer();
        if(hasTimerCD && !hasTimer) user.startTimerTickingDown(timeInSeconds); //if some how both hasTimer and hasTimerCD are true, then it doesn't fire either timer

        if(!crownEvent.getSetting(CrownBooleanSettings.CUMULATIVE_POINTS)) user.addToScoreMap();
        user.setInEvent(true);
        user.teleportToEventStart();
    }

    // Some generic stuffs again, but for when a player leaves an event.
    @EventHandler
    public void onEventLeave(CrownLeaveEvent event){
        if(event.isCancelled()) return;

        CrownEventUser user = event.getUser();
        Score score = user.getCrownScore();
        Player player = user.getPlayer();
        String congratsMessage = crownEvent.getMessage(CrownMessages.SCORE_FINISH).replaceAll("%SCORE%", String.valueOf(event.getFinalScore()));

        if(!crownEvent.getSetting(CrownBooleanSettings.CUMULATIVE_POINTS) && !event.getHasTimer() && !event.getHasTimerCD()){
            if(user.isRecordSmallerThanScore()) {
                score.setScore(user.getScoreMapScore());
                user.removeFromScoreMap();
            }
        } else score.setScore(event.getFinalScore());

        if(event.getHasTimer()){
            user.stopTimer();
            congratsMessage = crownEvent.getMessage(CrownMessages.TIMER_FINISH);
            congratsMessage = congratsMessage.replaceAll("%TIMELEFT%", crownEvent.getTimerString(event.getTimerScore()).toString());
            if(event.getTimerScore() > user.getCrownScore().getScore()) user.getCrownScore().setScore((int) event.getTimerScore());
        }
        if(event.getHasTimerCD()){
            user.stopTimer();
            congratsMessage = crownEvent.getMessage(CrownMessages.TIMELEFT_FINISH);
            congratsMessage = congratsMessage.replaceAll("%TIMELEFT%", crownEvent.getTimerString(event.getTimerScore()).toString());
            if(event.getTimerScore() < user.getCrownScore().getScore()) user.getCrownScore().setScore((int) event.getTimerScore());
        }

        player.sendMessage(congratsMessage);
        user.setInEvent(false);
        if(crownEvent.getSetting(CrownBooleanSettings.TO_SPAWN_ON_END)) user.teleportToHazelguard();
        else user.teleportToEventLobby();
    }
}
