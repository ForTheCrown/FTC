package net.forthecrown.vikings.raids.valhalla;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RaidManager {

    //Todo exitLocation is currently a placeholder for the actual exit location
    protected static final Location EXIT_LOCATION = new Location(Bukkit.getWorld("world_void"), -509, 4, -493);
    private static final Map<String, VikingRaid> VIKING_RAIDS = new HashMap<>();
    private static final Map<String, RaidParty> ACTIVE_RAIDS = new HashMap<>();
    private final Server server;

    public RaidManager(Server server){
        this.server = server;
    }

    public void registerParty(String name, RaidParty party){
        ACTIVE_RAIDS.put(name, party);
    }

    public void removeParty(String s){
        ACTIVE_RAIDS.remove(s);
    }

    public RaidParty partyFromName(String s){
        if(ACTIVE_RAIDS.containsKey(s)) return ACTIVE_RAIDS.get(s);
        return null;
    }

    public static VikingRaid fromName(String name){
        if(VIKING_RAIDS.containsKey(name)) return VIKING_RAIDS.get(name);
        return null;
    }

    public void registerRaid(VikingRaid vikingRaid){
        VIKING_RAIDS.put(vikingRaid.getName(), vikingRaid);
    }

    public Collection<VikingRaid> getRaids(){
        return VIKING_RAIDS.values();
    }

    public Map<String, VikingRaid> getKnownRaids(){
        return VIKING_RAIDS;
    }

    public void unregisterRaid(VikingRaid vikingRaid){
        VIKING_RAIDS.remove(vikingRaid);
    }

    public Location getExitLocation() {
        return EXIT_LOCATION;
    }

    public void callRaid(RaidParty party){
        party.selectedRaid.initRaid(party);
    }
}
