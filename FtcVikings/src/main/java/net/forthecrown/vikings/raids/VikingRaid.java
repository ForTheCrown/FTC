package net.forthecrown.vikings.raids;

import net.forthecrown.vikings.Vikings;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;

public abstract class VikingRaid implements Listener {

    protected final Location raidLocation;
    protected final String name;
    protected final Server server;

    protected Player usingPlayer;
    protected RaidDifficulty difficulty = RaidDifficulty.NORMAL;

    protected VikingRaid(Location raidLocation, String name, Server server){
        this.raidLocation = raidLocation;
        this.name = name;
        this.server = server;
    }

    public RaidDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(RaidDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void initRaid(Player player, RaidDifficulty difficulty) {
        this.difficulty = difficulty;
        this.usingPlayer = player;

        server.getPluginManager().registerEvents(this, Vikings.getInstance());

        onRaidLoad();
        player.teleport(getRaidLocation());
    }

    public abstract void onRaidLoad();
    public abstract void onRaidComplete();

    public void onRaidEnd() {
        getUsingPlayer().teleport(Vikings.getRaidHandler().getExitLocation());
        setUsingPlayer(null);

        HandlerList.unregisterAll(this);
    }

    public Location getRaidLocation() {
        return raidLocation;
    }

    public void setUsingPlayer(@Nullable Player player) {
        usingPlayer = player;
    }

    public Player getUsingPlayer() {
        return usingPlayer;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AbstractRaid{name=" + name + "}";
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if(getUsingPlayer() == null) return;
        if(!event.getEntity().equals(getUsingPlayer())) return;

        onRaidEnd();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(getUsingPlayer() == null) return;
        if(!event.getPlayer().equals(getUsingPlayer())) return;

        onRaidEnd();
    }
}
