package net.forthecrown.vikings.raids.valhalla;

import net.forthecrown.core.api.Announcer;
import net.forthecrown.core.api.CrownUser;
import net.forthecrown.core.api.UserDataContainer;
import net.forthecrown.core.api.UserManager;
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

//Class existence reason: Be a generic raid that can be extended, and which acts as the center point for all the raid stuff
public abstract class VikingRaid implements Listener {

    protected final Location raidLocation;
    protected final String name;
    protected final Server server;
    protected RaidAreaGenerator generator;
    protected RaidParty usingParty;
    protected boolean inUse;

    protected VikingRaid(Location raidLocation, String name, Server server){
        this.raidLocation = raidLocation;
        this.name = name;
        this.server = server;
    }

    public void initRaid(RaidParty party) {
        setCurrentParty(party);

        generator.generate();
        server.getPluginManager().registerEvents(this, Vikings.getInstance());

        onLoad();
        enterRaid();
    }

    public void enterRaid(){
        getCurrentParty().teleport(getRaidLocation());
        onEnter();
    }

    protected void onLoad() {}
    protected void onComplete() {}
    protected void onLose() {end();}
    protected void onEnter() {}
    protected void onEnd() {}

    public void end() {
        onEnd();

        getCurrentParty().teleport(RaidManager.EXIT_LOCATION);
        Announcer.ac("sadf");
        setCurrentParty(null);

        HandlerList.unregisterAll(this);
    }

    public void completeRaid(){
        for (Player p: getCurrentParty()){
            CrownUser u = UserManager.getUser(p);
            UserDataContainer dataContainer = u.getDataContainer();
            ConfigurationSection section = dataContainer.get(Vikings.getInstance());
            List<String> completedLevels = section.getStringList("CompletedLevels");

            completedLevels.add(getName());
            dataContainer.set(Vikings.getInstance(), section);
        }

        onComplete();
        end();
    }

    public Location getRaidLocation() {
        return raidLocation;
    }

    public void setCurrentParty(@Nullable RaidParty party) {
        usingParty = party;
    }

    public RaidParty getCurrentParty() {
        return usingParty;
    }

    public String getName() {
        return name;
    }

    public RaidAreaGenerator getGenerator() {
        return generator;
    }

    public boolean isInUse() {
        return inUse;
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
        if(getCurrentParty() == null) return;
        if(!getCurrentParty().getParticipants().contains(event.getEntity())) return;

        getCurrentParty().leaveParty(event.getEntity());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if(getCurrentParty() == null) return;
        if(!getCurrentParty().getParticipants().contains(event.getPlayer())) return;

        getCurrentParty().leaveParty(event.getPlayer());
    }
}
