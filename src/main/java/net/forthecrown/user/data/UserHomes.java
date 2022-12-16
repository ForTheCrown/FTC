package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import net.forthecrown.core.FTC;
import net.forthecrown.core.Permissions;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.user.ComponentType;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import net.forthecrown.utils.text.Text;
import net.forthecrown.waypoint.Waypoint;
import net.forthecrown.waypoint.WaypointManager;
import net.forthecrown.waypoint.Waypoints;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class UserHomes extends UserComponent {
    private static final Logger LOGGER = FTC.getLogger();

    /* ----------------------------- CONSTANTS ------------------------------ */

    /**
     * To make implementing region homes easy, I simply added
     * an element with this key into the home JSON object.
     * <p>
     * The home region is serialized with this string
     * as the key next to all the user's other named homes.
     */
    public static final String HOME_WAYPOINT_JSON_NAME = "user:home:waypoint";

    /**
     * Name of the default user home that, if given no home name, commands
     * will default to using.
     */
    public static final String DEFAULT = "home";

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /**
     * The name 2 location map of all of this user's homes.
     */
    @Getter
    private final Map<String, Location> homes = new HashMap<>();

    /** The ID of the user's home waypoint */
    @Getter
    private UUID homeWaypoint;

    /* ----------------------------- CONSTRUCTOR ------------------------------ */

    public UserHomes(User user, ComponentType<UserHomes> type) {
        super(user, type);
    }

    /* ----------------------------- STATIC UTILITY ------------------------------ */

    static void reassignWaypointHome(UUID uuid,
                                     @Nullable UUID old,
                                     @Nullable Waypoint newWaypoint
    ) {
        if (newWaypoint != null) {
            if (Objects.equals(old, newWaypoint.getId())) {
                return;
            }
        } else if (old == null) {
            return;
        }

        var manager = WaypointManager.getInstance();

        if (old != null) {
            var wayOld = manager.get(old);

            if (wayOld != null) {
                wayOld.removeResident(uuid);
                Waypoints.removeIfPossible(wayOld);
            }
        }

        if (newWaypoint != null) {
            newWaypoint.addResident(uuid);
        }
    }

    /* ----------------------------- METHODS ------------------------------ */

    /**
         * Clears all the user's homes
         */
    public void clear() {
        homes.clear();
    }

    /**
         * Gets the amount of homes this user has
         * @return The amount of homes
         */
    public int size() {
        return homes.size();
    }

    /**
         * Checks if the user has a home by the given name
         * @param name The name to check for
         * @return First line says it bruv
         */
    public boolean contains(String name) {
        return homes.containsKey(name);
    }

    /**
     * Checks whether the user is allowed to make more homes
     * <p>
     * If the user to whom these homes belong to is Opped,
     * then this method will always return true.
     * @return Whether the user is allowed to create new homes
     */
    public boolean canMakeMore() {
        return size() < Permissions.MAX_HOMES.getTier(user);
    }

    /**
         * Gets whether the user has any homes at all
         * @return Just get a hoos
         */
    public boolean isEmpty() {
        return homes.isEmpty();
    }

    /**
         * Sets a home with the given name and location
         * @param name Name of the home
         * @param location Location of the home
         */
    public void set(String name, Location location) {
        homes.put(name, location);
    }

    /**
         * Removes a home with the given name
         * @param name The name to remove
         */
    public void remove(String name) {
        homes.remove(name);
    }

    /**
         * Gets a home's location by the given name
         * @param name The name to get the location of
         * @return The location, null, if no home exists by the give name
         */
    public Location get(String name) {
        var l = homes.get(name);
        return l == null ? null : l.clone();
    }

    /**
     * Sets the user's home waypoint
     * @param homeWaypoint The new home waypoint
     */
    public void setHomeWaypoint(@Nullable Waypoint homeWaypoint) {
        UUID old = this.homeWaypoint;

        if (homeWaypoint == null) {
            this.homeWaypoint = null;
        } else {
            this.homeWaypoint = homeWaypoint.getId();
        }

        reassignWaypointHome(getUser().getUniqueId(), old, homeWaypoint);
    }

    public Waypoint getHomeTeleport() {
        if (!hasHomeWaypoint()) {
            return null;
        }

        return WaypointManager
                .getInstance()
                .get(getHomeWaypoint());
    }

    @Override
    public JsonObject serialize() {
        if (homes.isEmpty()) {
            return null;
        }

        JsonWrapper json = JsonWrapper.create();
        if (hasHomeWaypoint()) {
            json.add(HOME_WAYPOINT_JSON_NAME, homeWaypoint.toString());
        }

        for (Map.Entry<String, Location> e : homes.entrySet()) {
            json.addLocation(e.getKey(), e.getValue());
        }

        return json.nullIfEmpty();
    }

    @Override
    public void deserialize(JsonElement element) {
        homes.clear();
        homeWaypoint = null;

        if (element == null) {
            return;
        }

        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());
        // Remove legacy region home pos
        json.remove("user:home:region");

        if (json.has(HOME_WAYPOINT_JSON_NAME)) {
            this.homeWaypoint = json.getUUID(HOME_WAYPOINT_JSON_NAME);
            json.remove(HOME_WAYPOINT_JSON_NAME);
        }

        for (Map.Entry<String, JsonElement> e : json.entrySet()) {
            homes.put(e.getKey(), JsonUtils.readLocation(e.getValue().getAsJsonObject()));
        }
    }

    /**
     * Suggests the homes this component holds
     * @param builder The builder to suggest to
     * @param prependUsername Whether to add the user's name
     *                        onto the beginning of the suggestions
     */
    public void suggestHomeNames(SuggestionsBuilder builder, boolean prependUsername) {
        var token = builder.getRemainingLowerCase();
        var prefix = prependUsername ? getUser().getNickOrName() + ":" : "";

        for (var e: homes.entrySet()) {
            var name = prefix + e.getKey();

            if (CompletionProvider.startsWith(token, name)) {
                var l = e.getValue();

                // Suggest name with the location as the hover text
                builder.suggest(
                        name,
                        CmdUtil.toTooltip(
                                Text.prettyLocation(l, true)
                        )
                );
            }
        }
    }

    /**
     * Gets whether the user has a set home region
     * @return Whether the user has a home region.
     */
    public boolean hasHomeWaypoint() {
        return getHomeWaypoint() != null;
    }
}