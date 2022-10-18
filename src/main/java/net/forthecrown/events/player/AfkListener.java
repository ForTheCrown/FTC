package net.forthecrown.events.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.forthecrown.core.AfkKicker;
import net.forthecrown.user.Users;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

/**
 * Listener for detecting if AFK players should be
 * un-AFK-ed or for pushing back the {@link AfkKicker}
 */
public class AfkListener implements Listener {
    public void checkUnafk(PlayerEvent event){
        checkUnafk(event.getPlayer());
    }

    public void checkUnafk(Player plr){
        var user = Users.getLoadedUser(plr.getUniqueId());

        if (user == null || !user.isAfk()) {
            return;
        }

        user.unafk();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if(!event.hasChangedBlock() && !event.hasChangedOrientation()) {
            return;
        }

        checkUnafk(event);
        AfkKicker.addOrDelay(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        checkUnafk(event);
        AfkKicker.addOrDelay(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        AfkKicker.addOrDelay(event.getPlayer().getUniqueId());

        if (!event.getMessage().startsWith("/afk") && !event.getMessage().startsWith("afk")) {
            checkUnafk(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        checkUnafk(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        checkUnafk(event.getEntity());
    }
}