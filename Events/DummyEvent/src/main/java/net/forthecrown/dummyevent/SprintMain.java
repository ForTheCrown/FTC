package net.forthecrown.dummyevent;

import net.forthecrown.core.commands.CommandLeave;
import net.forthecrown.core.crownevents.ArmorStandLeaderboard;
import net.forthecrown.core.crownevents.ObjectiveLeaderboard;
import net.forthecrown.dummyevent.commands.CommandSprint;
import net.forthecrown.dummyevent.events.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public final class SprintMain extends JavaPlugin {

    public static SprintMain plugin;
    public static SprintEvent event;
    public static ObjectiveLeaderboard leaderboard;

    @Override
    public void onEnable() {
        plugin = this;

        event = new SprintEvent();
        new CommandSprint();
        createLeaderboard();

        CommandLeave.addAllowedArea(SprintEvent.RACE_AREA, SprintEvent.EXIT_LOCATION, player -> {
            if(!event.isInEvent(player)) return;
            event.endAndRemove(SprintEvent.PARTICIPANTS.get(player));
        });

        getServer().getPluginManager().registerEvents(new EventListener(), this);
    }

    public void createLeaderboard(){
        leaderboard = new ObjectiveLeaderboard("&6Sprint times", SprintEvent.CROWN, new Location(Bukkit.getWorld("world_void"), -586, 107, 457));
        leaderboard.setTimerScore(true);
        leaderboard.setOrder(ArmorStandLeaderboard.Order.LOW_TO_HIGH);
        leaderboard.setFormat("&e%pos. &r%name: &e%score");
        leaderboard.update();
    }

    @Override
    public void onDisable() {
        event.clear();
    }
}
