package ftc.randomfeatures;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TempEvent implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if(!Main.valentinesPlayer.contains(e.getPlayer().getUniqueId())) return;

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user " + e.getPlayer().getName() + " permission set ftc.emotes.hug true");
        Main.valentinesPlayer.remove(e.getPlayer().getUniqueId());
    }
}
