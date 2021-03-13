package net.forthecrown.vikings.raids;

import net.forthecrown.core.FtcCore;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.vikings.Vikings;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

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

    protected abstract void onRaidLoad();
    protected abstract void onRaidComplete();

    public void onRaidEnd() {
        getUsingPlayer().teleport(Vikings.getRaidHandler().getExitLocation());
        setUsingPlayer(null);

        HandlerList.unregisterAll(this);
    }

    public void completeRaid(){
        CrownUser user = FtcCore.getUser(getUsingPlayer());

        ConfigurationSection dataSec = user.getDataContainer().get(Vikings.getInstance());
        List<String> completedRaids = dataSec.getStringList("CompletedRaids");
        completedRaids.add(getName());

        dataSec.set("CompletedRaids", completedRaids);
        user.getDataContainer().set(Vikings.getInstance(), dataSec);

        onRaidComplete();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VikingRaid raid = (VikingRaid) o;
        return getRaidLocation().equals(raid.getRaidLocation()) &&
                getName().equals(raid.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRaidLocation(), getName());
    }

    @Override
    public String toString() {
        return getClass().getName() + "{name=" + name + "}";
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
