package net.forthecrown.emperor.user;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FtcUserHomes implements UserHomes {
    public final Map<String, Location> homes = new HashMap<>();
    private final FtcUser owner;

    public FtcUserHomes(FtcUser user){
        this.owner = user;
    }

    public void saveInto(ConfigurationSection section){
        for (Map.Entry<String, Location> e: homes.entrySet()){
            section.set(e.getKey(), e.getValue());
        }
    }

    public void loadFrom(ConfigurationSection section){
        homes.clear();
        for (String s: section.getKeys(false)){
            homes.put(s, section.getLocation(s));
        }
    }

    @Override
    public void clear() {
        homes.clear();
    }

    @Override
    public int size(){
        return homes.size();
    }

    @Override
    public boolean contains(String name){
        return homes.containsKey(name);
    }

    @Override
    public boolean canMakeMore(){
        return owner.getHighestTierRank().tier.maxHomes >= size();
    }

    @Override
    public boolean isEmpty() {
        return homes.isEmpty();
    }

    @Override
    public Map<String, Location> getHomes() {
        return homes;
    }

    @Override
    public CrownUser getOwner() {
        return owner;
    }

    @Override
    public Set<String> getHomeNames(){
        return homes.keySet();
    }

    @Override
    public Collection<Location> getHomeLocations(){
        return homes.values();
    }

    @Override
    public void set(String name, Location location){
        homes.put(name, location);
    }

    @Override
    public void remove(String name){
        homes.remove(name);
    }

    @Override
    public Location get(String name){
        return homes.get(name);
    }
}
