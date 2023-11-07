package net.forthecrown.core.user;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.core.CorePermissions;
import net.forthecrown.grenadier.Completions;
import net.forthecrown.grenadier.Grenadier;
import net.forthecrown.text.Text;
import net.forthecrown.user.ComponentName;
import net.forthecrown.user.User;
import net.forthecrown.user.UserComponent;
import net.forthecrown.utils.Locations;
import net.forthecrown.utils.io.JsonUtils;
import net.forthecrown.utils.io.JsonWrapper;
import org.bukkit.Location;

@ComponentName("homes")
public class UserHomes implements UserComponent {
  /* ----------------------------- CONSTANTS ------------------------------ */

  /**
   * To make implementing region homes easy, I simply added an element with this key into the home
   * JSON object.
   * <p>
   * The home region is serialized with this string as the key next to all the user's other named
   * homes.
   */
  public static final String HOME_WAYPOINT_JSON_NAME = "user:home:waypoint";

  /**
   * Name of the default user home that, if given no home name, commands will default to using.
   */
  public static final String DEFAULT = "home";

  /* -------------------------- INSTANCE FIELDS --------------------------- */

  /**
   * The name 2 location map of all of this user's homes.
   */
  @Getter
  private final Map<String, Location> homes = new HashMap<>();

  /**
   * The ID of the user's home waypoint
   */
  @Getter
  private UUID homeWaypoint;

  @Getter
  private final User user;

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  public UserHomes(User user) {
    this.user = user;
  }

  /* ------------------------------ METHODS ------------------------------- */

  /**
   * Clears all the user's homes
   */
  public void clear() {
    homes.clear();
  }

  /**
   * Gets the amount of homes this user has
   *
   * @return The amount of homes
   */
  public int size() {
    return homes.size();
  }

  /**
   * Checks if the user has a home by the given name
   *
   * @param name The name to check for
   * @return First line says it bruv
   */
  public boolean contains(String name) {
    return homes.containsKey(name);
  }

  /**
   * Checks whether the user is allowed to make more homes
   * <p>
   * If the user to whom these homes belong to is Opped, then this method will always return true.
   *
   * @return Whether the user is allowed to create new homes
   */
  public boolean canMakeMore() {
    return size() < getMaxHomes();
  }

  public int getMaxHomes() {
    var perm = CorePermissions.MAX_HOMES;
    return perm.getTier(user).orElse(perm.getMinTier());
  }

  public void removeOverMax() {
    if (!user.isOnline()) {
      return;
    }

    if (CorePermissions.MAX_HOMES.hasUnlimited(user)) {
      return;
    }

    homes.entrySet().removeIf(home -> {
      int size = homes.size();

      if (size > getMaxHomes()) {
        Loggers.getLogger().debug("Removing home {} from {}, over max",
            home.getKey(), user
        );

        return true;
      }

      return false;
    });
  }

  /**
   * Gets whether the user has any homes at all
   */
  public boolean isEmpty() {
    return homes.isEmpty();
  }

  /**
   * Sets a home with the given name and location
   *
   * @param name     Name of the home
   * @param location Location of the home
   */
  public void set(String name, Location location) {
    Objects.requireNonNull(name, "Null name");
    Objects.requireNonNull(location, "Null location");
    homes.put(name, Locations.clone(location));
  }

  /**
   * Removes a home with the given name
   *
   * @param name The name to remove
   */
  public void remove(String name) {
    homes.remove(name);
  }

  /**
   * Gets a home's location by the given name
   *
   * @param name The name to get the location of
   * @return The location, null, if no home exists by the give name
   */
  public Location get(String name) {
    var l = homes.get(name);
    return Locations.clone(l);
  }

  @Override
  public JsonObject serialize() {
    removeOverMax();

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
    removeOverMax();
  }

  /**
   * Suggests the homes this component holds
   * @param builder The builder to suggest to
   */
  public void suggestHomeNames(SuggestionsBuilder builder) {
    var token = builder.getRemainingLowerCase();

    for (var e : homes.entrySet()) {
      var name = e.getKey();

      if (Completions.matches(token, name)) {
        var l = e.getValue();

        // Suggest name with the location as the hover text
        builder.suggest(name, Grenadier.toMessage(Text.prettyLocation(l, true)));
      }
    }
  }

  /**
   * Gets whether the user has a set home region
   *
   * @return Whether the user has a home region.
   */
  public boolean hasHomeWaypoint() {
    return getHomeWaypoint() != null;
  }
}