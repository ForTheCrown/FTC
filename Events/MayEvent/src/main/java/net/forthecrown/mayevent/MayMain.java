package net.forthecrown.mayevent;

import net.forthecrown.core.CrownBoundingBox;
import net.forthecrown.core.commands.CommandLeave;
import net.forthecrown.core.crownevents.ObjectiveLeaderboard;
import net.forthecrown.core.crownevents.reporters.EventReporter;
import net.forthecrown.core.crownevents.reporters.ReporterFactory;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.grenadier.RoyalArguments;
import net.forthecrown.grenadier.VanillaArgumentType;
import net.forthecrown.mayevent.command.CommandMayEvent;
import net.forthecrown.mayevent.command.WeaponArgType;
import net.forthecrown.mayevent.events.MayListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class MayMain extends JavaPlugin {

    public static MayMain inst;
    public static DoomEvent event;
    public static ObjectiveLeaderboard leaderboard;
    public static EventReporter eLogger;

    @Override
    public void onEnable() {
        inst = this;
        event = new DoomEvent.Impl();
        eLogger = ReporterFactory.of(this, event);

        RoyalArguments.register(WeaponArgType.class, VanillaArgumentType.WORD);

        new CommandMayEvent();

        CommandLeave.add(new CrownBoundingBox(DoomEvent.EVENT_WORLD, -30000000, 0, -30000000, 30000000, 256, 30000000),
                DoomEvent.EXIT_LOCATION, player -> {
                    boolean result = DoomEvent.ENTRIES.containsKey(player);
                    if(result) event.end(DoomEvent.ENTRIES.get(player));

                    return result;
                }
        );

        regEvents(getServer().getPluginManager());
        createLeaderboard();
    }

    public void createLeaderboard(){
        leaderboard = new ObjectiveLeaderboard("Event scores", DoomEvent.CROWN, new Location(CrownUtils.WORLD, -47.5, 70.5, 879.5));
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
        Bukkit.getScheduler().cancelTasks(this);

        try {
            eLogger.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
