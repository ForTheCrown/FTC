package net.forthecrown.marchevent;

import net.forthecrown.marchevent.commands.CrownGameCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class EventMain extends JavaPlugin {

    private static EventMain plugin;
    private static PvPEvent event;

    @Override
    public void onEnable() {
        plugin = this;

        new CrownGameCommand();
        event = new PvPEvent();
    }

    @Override
    public void onDisable() {
        for (Player p: PvPEvent.inEvent){
            p.teleport(PvPEvent.EXIT_LOCATION);
        }
    }

    public static EventMain getInstance(){
        return plugin;
    }

    public static PvPEvent getEvent() {
        return event;
    }
}
