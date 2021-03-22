package net.forthecrown.easteregghunt;

import net.forthecrown.core.crownevents.ObjectiveLeaderboard;
import net.forthecrown.easteregghunt.commands.CommandEasterEgg;
import net.forthecrown.easteregghunt.events.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class EasterMain extends JavaPlugin {

    public static EasterMain instance;

    public static List<Location> eggSpawns = new ArrayList<>();
    public static EasterEvent event;
    public static EggSpawner spawner;
    public static ObjectiveLeaderboard leaderboard;

    public static final Location EXIT_LOCATION = new Location(Bukkit.getWorld("world_void"), 10, 10, 10);

    @Override
    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        eggSpawns = (List<Location>) getConfig().getList("SpawnLocations");

        //event
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        //command
        new CommandEasterEgg();

        spawner = new EggSpawner();
        event = new EasterEvent(this, spawner);
    }

    private void createLeaderboard(){
        leaderboard = new ObjectiveLeaderboard("Easter times",
                EasterEvent.CROWN,
                new Location(Bukkit.getWorld("world_void"), 10, 10, 10));

        leaderboard.setFormat("&e%pos. &r%name: &e%score");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
