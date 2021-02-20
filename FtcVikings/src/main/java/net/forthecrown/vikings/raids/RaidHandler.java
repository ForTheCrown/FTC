package net.forthecrown.vikings.raids;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class RaidHandler {

    //CHANGE: exitLocation is currently a placeholder for the actual exit location
    protected static final Location exitLocation = new Location(Bukkit.getWorld("world"), 100, 100, 100);
    private static final Set<VikingRaid> VIKING_RAIDS = new HashSet<>();
    private final Server server;

    public RaidHandler(Server server){
        this.server = server;
    }

    public static VikingRaid fromName(String name){
        for (VikingRaid r : VIKING_RAIDS) if(r.getName().equals(name)) return r;
        return null;
    }

    public static VikingRaid fromPlayer(Player player){
        for (VikingRaid r : VIKING_RAIDS){
            if(r.getUsingPlayer() == null) continue;
            if(r.getUsingPlayer().equals(player)) return r;
        }
        return null;
    }

    public void registerRaid(VikingRaid vikingRaid){
        VIKING_RAIDS.add(vikingRaid);
    }

    public Set<VikingRaid> getRaids(){
        return VIKING_RAIDS;
    }

    public void unregisterRaid(VikingRaid vikingRaid){
        VIKING_RAIDS.remove(vikingRaid);
    }

    public Location getExitLocation() {
        return exitLocation;
    }

    public void callRaid(Player player, VikingRaid vikingRaid){
        callRaid(vikingRaid, player, RaidDifficulty.NORMAL);
    }

    public void callRaid(VikingRaid vikingRaid, Player player, RaidDifficulty difficulty){
        vikingRaid.initRaid(player, difficulty);
    }
}
