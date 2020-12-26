package ftc.crownapi;

import ftc.crownapi.apievents.CrownEnterEvent;
import ftc.crownapi.apievents.CrownLeaveEvent;
import ftc.crownapi.settings.CrownBooleanSettings;
import ftc.crownapi.settings.CrownSettings;
import ftc.crownapi.types.CrownEvent;
import ftc.crownapi.types.CrownEventUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class APIListener extends CrownEvent implements Listener {

    private final CrownEvent crownMain = new CrownEvent();

    //TP's a player out of the event if they've left while in the event
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(!CrownSettings.getSetting(CrownBooleanSettings.REMOVE_ON_SHUTDOWN) && !crownMain.getPlayersThatQuitInEvent().contains(player)) return;
        CrownEventUser user = new CrownEventUser(player);

        if(CrownSettings.getSetting(CrownBooleanSettings.TO_SPAWN_ON_END)){
            user.teleportToHazelguard();
        } else {
            user.teleportToEventLobby();
        }
        user.setHasQuitInEvent(false);
    }

    // Adds player to the LeftInEvent players list when they leave
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        CrownEventUser user = new CrownEventUser(player);
        if(!user.isInEvent()) return;
        user.setHasQuitInEvent(true);
        user.setInEvent(false);
        user.removeFromScoreMap();
    }

    // Keep in mind: These custom events have to be called on in the event plugins themselves. They don't fire whenever lol

    // Some generic stuffs for when a player enters an event
    @EventHandler
    public void onEventEnter(CrownEnterEvent event){
        if(event.isCancelled()) return;

        CrownEventUser user = event.getUser();
        if(user.isDisqualified()){
            event.setCancelled(true);
            return;
        }
        if(!CrownSettings.getSetting(CrownBooleanSettings.CUMULATIVE_POINTS)) user.addToScoreMap();
        user.setInEvent(true);
    }

    // Some generic stuffs again, but for when a player leaves an event.
    @EventHandler
    public void onEventLeave(CrownLeaveEvent event){
        if(event.isCancelled()) return;
    }
}
