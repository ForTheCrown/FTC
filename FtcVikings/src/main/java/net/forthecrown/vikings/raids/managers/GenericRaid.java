package net.forthecrown.vikings.raids.managers;

import net.forthecrown.vikings.Vikings;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

public abstract class GenericRaid implements VikingRaid, Listener {

    protected final Location raidLocation;
    protected final String name;
    protected Player usingPlayer;
    protected RaidDifficulty difficulty = RaidDifficulty.NORMAL;
    protected PluginManager manager;

    protected GenericRaid(Location raidLocation, String name){
        this.raidLocation = raidLocation;
        this.name = name;
    }

    @Override
    public RaidDifficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public void setDifficulty(RaidDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public void raidInit(Player player, RaidDifficulty difficulty, PluginManager manager) {
        this.difficulty = difficulty;
        this.usingPlayer = player;

        this.manager = manager;
        manager.registerEvents(this, Vikings.getInstance());

        onRaidLoad();
        player.teleport(getRaidLocation());
    }

    @Override
    public void onRaidEnd() {
        getUsingPlayer().teleport(Vikings.getRaidHandler().getExitLocation());
        setUsingPlayer(null);

        HandlerList.unregisterAll(this);
    }

    public Location getRaidLocation() {
        return raidLocation;
    }

    @Override
    public void setUsingPlayer( Player player) {
        usingPlayer = player;
    }

    @Override
    public Player getUsingPlayer() {
        return usingPlayer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
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

        getUsingPlayer().teleport(Vikings.getRaidHandler().getExitLocation());
    }
}
