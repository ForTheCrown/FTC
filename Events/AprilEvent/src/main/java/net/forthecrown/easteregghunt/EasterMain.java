package net.forthecrown.easteregghunt;

import net.forthecrown.core.commands.CommandLeave;
import net.forthecrown.core.crownevents.ObjectiveLeaderboard;
import net.forthecrown.core.utils.CrownUtils;
import net.forthecrown.easteregghunt.commands.CommandEasterEgg;
import net.forthecrown.easteregghunt.events.EventListener;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class EasterMain extends JavaPlugin {

    public static EasterMain inst;

    public static List<Location> eggSpawns = new ArrayList<>();
    public static EasterEvent event;
    public static EggSpawner spawner;
    public static ObjectiveLeaderboard leaderboard;
    public static CrazyBunny bunny;
    private static UserTracker tracker;

    @Override
    public void onEnable() {
        inst = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        //event
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        //command
        new CommandEasterEgg();


        tracker = new UserTracker();
        bunny = new CrazyBunny();
        spawner = new EggSpawner();
        event = new EasterEvent(this, spawner);

        CommandLeave.addAllowedArea(EasterEvent.EVENT_AREA, EasterEvent.EXIT_LOCATION, player -> {
            EasterEvent.shouldCancel = true;
            if(event.entry == null) return false;
            if(!event.entry.player().equals(player)) return false;
            event.end(event.entry);
            return true;
        });

        createLeaderboard();
    }

    private void createLeaderboard(){
        leaderboard = new ObjectiveLeaderboard("Easter times",
                EasterEvent.CROWN,
                new Location(CrownUtils.WORLD_VOID, -615.5, 105.5, 263.5));

        leaderboard.setFormat("&e%pos. &r%name: &e%score");
        leaderboard.update();
    }

    public static IUserTracker tracker(){
        return tracker;
    }

    @Override
    public void saveConfig() {
        getConfig().set("SpawnLocations", eggSpawns);
        super.saveConfig();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        eggSpawns = (List<Location>) getConfig().getList("SpawnLocations");
    }

    @Override
    public void onDisable() {
        saveConfig();
        if(!EasterEvent.open) event.end(event.entry);
        tracker.save();
    }
}
