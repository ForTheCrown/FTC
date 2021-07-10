package net.forthecrown.user;

import org.bukkit.Location;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents everything about a user's homes
 */
public interface UserHomes extends UserAttachment {
    void clear();

    int size();

    boolean contains(String name);

    boolean canMakeMore();

    boolean isEmpty();

    Map<String, Location> getHomes();

    Set<String> getHomeNames();

    Collection<Location> getHomeLocations();

    void set(String name, Location location);

    void remove(String name);

    Location get(String name);
}
