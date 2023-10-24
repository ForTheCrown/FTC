package net.forthecrown.waypoints;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.collision.WorldChunkMap;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.waypoints.event.WaypointRemoveEvent;
import net.forthecrown.waypoints.type.WaypointType;
import org.slf4j.Logger;

public class WaypointManager {

  private static final Logger LOGGER = Loggers.getLogger();

  @Getter
  static WaypointManager instance;

  private final Path path;

  @Getter
  private final WaypointsPlugin plugin;

  private final Map<String, Waypoint> byName = new Object2ObjectOpenHashMap<>();
  private final Map<UUID, Waypoint> byId = new Object2ObjectOpenHashMap<>();

  @Getter
  final WorldChunkMap<Waypoint> chunkMap = new WorldChunkMap<>();

  final Map<String, WaypointExtension> extensions = new HashMap<>();

  /* ---------------------------- CONSTRUCTOR ----------------------------- */

  WaypointManager(WaypointsPlugin plugin) {
    this.path = PathUtil.pluginPath("waypoints.dat");
    this.plugin = plugin;
  }

  /* ------------------------------ METHODS ------------------------------- */

  public WaypointConfig config() {
    return plugin.wConfig;
  }

  public void addExtension(String name, WaypointExtension extension) {
    Objects.requireNonNull(name, "Null name");
    Objects.requireNonNull(extension, "Null extension");

    Preconditions.checkState(
        !extensions.containsKey(name),
        "Extension with name '%s' already registered", name
    );

    extensions.put(name, extension);
  }

  public void removeExtension(String name) {
    Objects.requireNonNull(name, "Null name");
    extensions.remove(name);
  }

  public Collection<WaypointExtension> getExtensions() {
    return extensions.values();
  }

  public void save() {
    SerializationHelper.writeTagFile(path, this::save);
  }

  public void load() {
    SerializationHelper.readTagFile(path, this::load);
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

    chunkMap.add(waypoint.getWorld(), waypoint.getBounds(), waypoint);
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
    waypoint.update(false);

    WaypointType type = waypoint.getType();
    if (type.isBuildable()) {
      Waypoints.clearPlatform(waypoint.getWorld(), waypoint.getPlatform());
    }

    type.onDelete(waypoint);

    waypoint.manager = null;
    byId.remove(waypoint.getId(), waypoint);
    chunkMap.remove(waypoint.getWorld(), waypoint);

    // If has name, remove from name lookup map
    var name = waypoint.get(WaypointProperties.NAME);
    if (!Strings.isNullOrEmpty(name)) {
      byName.remove(name.toLowerCase(), waypoint);
    }

    // If dynmap installed, remove marker
    WaypointWebmaps.removeMarker(waypoint);

    // If waypoint has residents, loop through them and
    // remove them from the waypoint
    if (!waypoint.getResidents().isEmpty()) {
      waypoint.clearResidents();
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

    for (var e: extensions.values()) {
      Waypoint w = e.lookup(name, this);

      if (w == null) {
        continue;
      }

      return w;
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

      try {
        waypoint.load(e.getValue().asCompound());
        addWaypoint(waypoint);
      } catch (Throwable t) {
        LOGGER.debug("Couldn't load waypoint {}", e.getKey(), t);
      }
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