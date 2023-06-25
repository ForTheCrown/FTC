package net.forthecrown.waypoints;

import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.user.Users;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.WorldChunkMap;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.waypoints.WaypointScan.Result;
import net.forthecrown.waypoints.event.WaypointQueryEvent;
import net.forthecrown.waypoints.event.WaypointRemoveEvent;
import org.slf4j.Logger;

public class WaypointManager {

  private static final Logger LOGGER = Loggers.getLogger();

  /**
   * The waypoint manager singleton instance
   */
  @Getter
  static WaypointManager instance;

  /**
   * Name lookup map
   */
  private final Map<String, Waypoint> byName = new Object2ObjectOpenHashMap<>();

  /**
   * ID lookup map
   */
  private final Map<UUID, Waypoint> byId = new Object2ObjectOpenHashMap<>();

  /**
   * Collision and spatial lookup map
   */
  @Getter
  final WorldChunkMap<Waypoint> chunkMap = new WorldChunkMap<>();

  private final Path path;

  private final WaypointsPlugin plugin;

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  WaypointManager(WaypointsPlugin plugin) {
    this.path = PathUtil.pluginPath("waypoints.dat");
    this.plugin = plugin;
  }

  /* ------------------------------ METHODS ------------------------------- */

  public WaypointConfig config() {
    return plugin.wConfig;
  }

  public void save() {
    SerializationHelper.writeTagFile(path, this::save);
  }

  public void load() {
    SerializationHelper.readTagFile(path, this::load);
  }

  public void onDayChange() {
    Map<Waypoint, Result> toRemove = new HashMap<>();

    for (var w : byId.values()) {
      Result result = WaypointScan.scan(w);

      if (result == Result.SUCCESS
          || result == Result.CANNOT_BE_DESTROYED
      ) {
        continue;
      }

      if (result == Result.DESTROYED) {
        toRemove.put(w, result);
        continue;
      }

      // Residents empty, no set name, no guild or pole was broken
      if (shouldRemove(w)) {
        toRemove.put(w, result);
      }
    }

    // No waypoints to remove so stop here
    if (toRemove.isEmpty()) {
      return;
    }

    // Remove all invalid waypoints
    toRemove.forEach((waypoint, result) -> {
      LOGGER.info("Auto-removing waypoint {}, reason={}",
          waypoint.identificationInfo(),
          result.getReason()
      );

      removeWaypoint(waypoint);
    });
  }

  private boolean shouldRemove(Waypoint waypoint) {
    if (waypoint.getLastValidTime() == -1) {
      waypoint.setLastValidTime(System.currentTimeMillis());
      return false;
    }

    long deletionDelay = config().waypointDeletionDelay.toMillis();
    long deletionTime = waypoint.getLastValidTime() + deletionDelay;

    return Time.isPast(deletionTime);
  }

  /**
   * Clears all waypoints
   */
  public void clear() {
    for (var w : byId.values()) {
      w.manager = null;
    }

    byName.clear();
    byId.clear();
    chunkMap.clear();
  }

  /**
   * Call back for when a region's name is changed to update the name lookup map
   */
  void onRename(Waypoint waypoint, String oldName, String newName) {
    if (!Strings.isNullOrEmpty(oldName)) {
      byName.remove(oldName.toLowerCase());
    }

    if (!Strings.isNullOrEmpty(newName)) {
      byName.put(newName.toLowerCase(), waypoint);
    }
  }

  public void addWaypoint(Waypoint waypoint) {
    waypoint.manager = this;
    byId.put(waypoint.getId(), waypoint);

    var name = waypoint.get(WaypointProperties.NAME);
    if (!Strings.isNullOrEmpty(name)) {
      byName.put(name.toLowerCase(), waypoint);

      // Ensure the marker exists, since we have a name
      Waypoints.updateDynmap(waypoint);
    }

    chunkMap.add(waypoint.getWorld(), waypoint);
  }

  /**
   * Fully removes the waypoint.
   * <p>
   * This will remove the waypoint from all lookup maps, remove its dynmap marker, unset all the
   * residents' home waypoint and unset the waypoint of any guild assigned to this waypoint.
   *
   * @param waypoint The waypoint to remove
   */
  public void removeWaypoint(Waypoint waypoint) {
    waypoint.getType().onDelete(waypoint);

    waypoint.manager = null;
    byId.remove(waypoint.getId());
    chunkMap.remove(waypoint.getWorld(), waypoint);

    // If has name, remove from name lookup map
    var name = waypoint.get(WaypointProperties.NAME);
    if (!Strings.isNullOrEmpty(name)) {
      byName.remove(name.toLowerCase());
      Waypoints.setNameSign(waypoint, null);
    }

    // If dynmap installed, remove marker
    WaypointDynmap.removeMarker(waypoint);

    // If waypoint has residents, loop through them and
    // remove them from the waypoint
    if (!waypoint.getResidents().isEmpty()) {
      waypoint.getResidents()
          .keySet()
          .stream()
          .map(Users::get)
          .forEach(user -> user.set(Waypoints.HOME_PROPERTY, null));
    }

    WaypointRemoveEvent event = new WaypointRemoveEvent(waypoint);
    event.callEvent();

/*
    // Unset guild's waypoint if there's a set guild owner
    UUID guildOwner = waypoint.get(WaypointProperties.GUILD_OWNER);
    if (guildOwner != null) {
      Guild guild = GuildManager.get()
          .getGuild(guildOwner);

      if (guild != null) {
        guild.getSettings()
            .setWaypoint(null);
      }

      Waypoints.setNameSign(waypoint, null);
    }*/
  }

  public Waypoint get(UUID uuid) {
    return byId.get(uuid);
  }

  public Waypoint get(String name) {
    return byName.get(name.toLowerCase());
  }

  public Waypoint getExtensive(String name) {
    Waypoint waypoint = get(name);

    if (waypoint != null) {
      return waypoint;
    }

    WaypointQueryEvent queryEvent = new WaypointQueryEvent(name);
    queryEvent.callEvent();

    if (queryEvent.getResult() != null) {
      return queryEvent.getResult();
    }

    try {
      UUID uuid = UUID.fromString(name);
      waypoint = get(uuid);
      return waypoint;
    } catch (IllegalArgumentException exc) {
      return null;
    }
  }

  public Stream<String> getNames() {
    // Don't return the keySet directly, as it contains
    // lowerCase versions of the name for ease of lookup
    return byName.values()
        .stream()
        .map(waypoint -> waypoint.get(WaypointProperties.NAME));
  }

  public Collection<Waypoint> getWaypoints() {
    return Collections.unmodifiableCollection(byId.values());
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  protected void load(CompoundTag tag) {
    clear();

    for (var e : tag.entrySet()) {
      Waypoint waypoint = new Waypoint(UUID.fromString(e.getKey()));
      waypoint.load(e.getValue().asCompound());

      addWaypoint(waypoint);
    }
  }

  protected void save(CompoundTag tag) {
    for (var w : byId.values()) {
      var cTag = BinaryTags.compoundTag();
      w.save(cTag);

      tag.put(w.getId().toString(), cTag);
    }
  }
}