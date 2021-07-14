package net.forthecrown.user;

import org.bukkit.Location;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents everything about a user's homes
 */
public interface UserHomes extends UserAttachment {

    /**
     * Clears all the user's homes
     */
    void clear();

    /**
     * Gets the amount of homes this user has
     * @return The amount of homes
     */
    int size();

    /**
     * Checks if the user has a home by the given name
     * @param name The name to check for
     * @return First line says it bruv
     */
    boolean contains(String name);

    /**
     * Checks whether the user is allowed to make more homes
     * @return Whether the user is allowed to create new homes
     */
    boolean canMakeMore();

    /**
     * Gets whether the user has any homes at all
     * @return Just get a hoos
     */
    boolean isEmpty();

    /**
     * Gets all the user's homes and their names
     * @return The user's homes
     */
    Map<String, Location> getHomes();

    /**
     * Gets all the home names
     * @return Home names
     */
    Set<String> getHomeNames();

    /**
     * Gets all the home locations
     * @return Home locations
     */
    Collection<Location> getHomeLocations();

    /**
     * Sets a home with the given name and location
     * @param name Name of the home
     * @param location Location of the home
     */
    void set(String name, Location location);

    /**
     * Removes a home with the given name
     * @param name The name to remove
     */
    void remove(String name);

    /**
     * Gets a home's location by the given name
     * @param name The name to get the location of
     * @return The location, null, if no home exists by the give name
     */
    Location get(String name);
}
