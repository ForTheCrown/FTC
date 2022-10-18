package net.forthecrown.user.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Getter;
import net.forthecrown.core.Permissions;
import net.forthecrown.text.Text;
import net.forthecrown.core.Crown;
import net.forthecrown.grenadier.CmdUtil;
import net.forthecrown.grenadier.CompletionProvider;
import net.forthecrown.regions.*;
import net.forthecrown.user.ComponentType;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserHomes extends UserComponent {
    private static final Logger LOGGER = Crown.logger();

    /* ----------------------------- CONSTANTS ------------------------------ */

    /**
     * To make implementing region homes easy, I simply added
     * an element with this key into the home JSON object.
     * <p>
     * The home region is serialized with this string
     * as the key next to all the user's other named homes.
     */
    public static final String HOME_REGION_JSON_NAME = "user:home:region";

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

    /**
     * The position of the region the user set as their home
     */
    @Getter
    public RegionPos homeRegion;

    /* ----------------------------- CONSTRUCTOR ------------------------------ */

    public UserHomes(User user, ComponentType<UserHomes> type) {
        super(user, type);
    }

    /* ----------------------------- STATIC UTILITY ------------------------------ */

    static void reassignHome(UUID uuid, String name, @Nullable Location old, @Nullable Location newHome) {
        if (old == null && newHome == null) {
            return;
        }

        RegionManager manager = RegionManager.get();

        /*if (inSameRegion(old, newHome, manager)) {
            return;
        }*/

        if (old != null && old.getWorld().equals(manager.getWorld())) {
            RegionPos pos = RegionPos.of(old);
            PopulationRegion region = manager.get(pos);

            region.getResidency().removeHome(uuid, name);

            if (!region.hasProperty(RegionProperty.HIDE_RESIDENTS)) {
                Regions.placePole(region);
            }
        }

        if (newHome != null && newHome.getWorld().equals(manager.getWorld())) {
            RegionPos pos = RegionPos.of(newHome);
            PopulationRegion region = manager.get(pos);

            region.getResidency().setHome(uuid, name);

            if (!region.hasProperty(RegionProperty.HIDE_RESIDENTS)) {
                Regions.placePole(region);
            }

            LOGGER.info("Set {}'s home '{}' to region: {}", uuid, name, pos);
        }
    }

    static void reassignRegionHome(UUID uuid, @Nullable RegionPos old, @Nullable RegionPos newPos) {
        RegionManager manager = RegionManager.get();

        if (old != null) {
            PopulationRegion region = manager.get(old);

            region.getResidency().moveOut(uuid);
            Regions.placePole(region);
        }

        if (newPos != null) {
            PopulationRegion region = manager.get(newPos);

            region.getResidency().moveIn(uuid);
            Regions.placePole(region);
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
        Location old = homes.put(name, location);

        reassignHome(getUser().getUniqueId(), name, location, old);
    }

    /**
         * Removes a home with the given name
         * @param name The name to remove
         */
    public void remove(String name) {
        Location old = homes.remove(name);

        reassignHome(getUser().getUniqueId(), name, old, null);
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
         * Sets the user's home region
         * @param cords The region cords at which their home will be.
         */
    public void setHomeRegion(RegionPos cords) {
        RegionPos old = this.homeRegion;
        this.homeRegion = cords;

        reassignRegionHome(getUser().getUniqueId(), old, cords);
    }

    @Override
    public JsonObject serialize() {
        if (homes.isEmpty()) {
            return null;
        }

        JsonWrapper json = JsonWrapper.create();
        if (hasHomeRegion()) {
            json.add(HOME_REGION_JSON_NAME, homeRegion.toString());
        }

        for (Map.Entry<String, Location> e : homes.entrySet()) {
            json.addLocation(e.getKey(), e.getValue());
        }

        return json.nullIfEmpty();
    }

    @Override
    public void deserialize(JsonElement element) {
        homes.clear();
        homeRegion = null;

        if (element == null) {
            return;
        }

        JsonWrapper json = JsonWrapper.wrap(element.getAsJsonObject());

        if (json.has(HOME_REGION_JSON_NAME)) {
            this.homeRegion = RegionPos.fromString(json.getString(HOME_REGION_JSON_NAME));
            json.remove(HOME_REGION_JSON_NAME);
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
    public boolean hasHomeRegion() {
        return getHomeRegion() != null;
    }
}