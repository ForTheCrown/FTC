package net.forthecrown.events.player;

import net.forthecrown.core.AfkKicker;
import net.forthecrown.core.TabList;
import net.forthecrown.user.User;
import net.forthecrown.user.UserManager;
import net.forthecrown.user.Users;
import net.forthecrown.user.packet.PacketListeners;
import net.forthecrown.user.property.Properties;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLeave(PlayerQuitEvent event){
        User user = Users.get(event.getPlayer());
        user.onLeave();

        PacketListeners.uninject(event.getPlayer());
        AfkKicker.remove(user.getUniqueId());
        event.quitMessage(null);

        if (!user.get(Properties.VANISHED)) {
            PlayerJoinListener.sendLogoutMessage(user);
        }

        UserManager.get().unload(user);
        TabList.update();
    }
}