package net.forthecrown.emperor.user;

import org.bukkit.Location;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface UserHomes {
    void clear();

    int size();

    boolean contains(String name);

    boolean canMakeMore();

    boolean isEmpty();

    Map<String, Location> getHomes();

    CrownUser getOwner();

    Set<String> getHomeNames();

    Collection<Location> getHomeLocations();

    void set(String name, Location location);

    void remove(String name);

    Location get(String name);
}
