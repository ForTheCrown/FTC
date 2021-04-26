package net.forthecrown.mayevent;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.commands.CommandLeave;
import net.forthecrown.core.crownevents.ObjectiveLeaderboard;
import net.forthecrown.mayevent.command.CommandMayEvent;
import net.forthecrown.mayevent.events.MayListener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MayMain extends JavaPlugin {

    public static MayMain inst;
    public static DoomEvent event;
    public static ObjectiveLeaderboard leaderboard;

    @Override
    public void onEnable() {
        inst = this;
        event = new DoomEvent.Impl();

        new CommandMayEvent();

        CommandLeave.add(new CrownBoundingBox(DoomEvent.EVENT_WORLD, -30000000, 0, -30000000, 30000000, 256, 30000000),
                DoomEvent.EXIT_LOCATION, plr -> {
                    boolean result = DoomEvent.ENTRIES.containsKey(plr);
                    if(result) event.end(DoomEvent.ENTRIES.get(plr));

                    return result;
                }
        );

        regEvents(getServer().getPluginManager());
        createLeaderboard();
    }

    public void createLeaderboard(){
        leaderboard = new ObjectiveLeaderboard("Event scores", DoomEvent.CROWN, DoomEvent.EXIT_LOCATION);
        leaderboard.create();
    }

    private void regEvents(PluginManager pm){
        pm.registerEvents(new MayListener(), this);
    }

    @Override
    public void onDisable() {
        for (ArenaEntry e: DoomEvent.ENTRIES.values()){
            event.end(e);
        }
    }
}
