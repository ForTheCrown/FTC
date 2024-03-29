package net.forthecrown.waypoints;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.utils.collision.WorldChunkMap;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.waypoints.event.WaypointRemoveEvent;
import net.forthecrown.waypoints.type.WaypointType;
import net.forthecrown.waypoints.util.MultiHomeFixer;
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
    WaypointHomes.clear();
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

  void onAliasesUpdate(Waypoint waypoint, List<String> old, List<String> newAliases) {
    if (old != null && !old.isEmpty()) {
      for (String s : old) {
        String normalized = s.toLowerCase();
        Waypoint found = byName.get(normalized);

        if (found == null || !Objects.equals(waypoint, found)) {
          continue;
        }

        byName.remove(normalized);
      }
    }

    if (newAliases == null || newAliases.isEmpty()) {
      return;
    }

    for (String newAlias : newAliases) {
      String label = newAlias.toLowerCase();
      Waypoint existing = byName.get(label);

      if (existing != null) {
        continue;
      }

      byName.put(label, waypoint);
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

    List<String> aliases = waypoint.get(WaypointProperties.ALIASES);
    if (aliases != null) {
      onAliasesUpdate(waypoint, null, aliases);
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

  public Set<String> getNames() {
    // Don't return the byName.keySet() directly, as it contains
    // lowerCase versions of the names and aliases for ease of lookup
    Set<String> names = new HashSet<>();

    for (Waypoint value : byId.values()) {
      String name = value.get(WaypointProperties.NAME);

      if (!Strings.isNullOrEmpty(name)) {
        names.add(name);
      }

      List<String> aliases = value.get(WaypointProperties.ALIASES);
      if (aliases != null) {
        names.addAll(aliases);
      }
    }

    return names;
  }

  public Collection<Waypoint> getWaypoints() {
    return Collections.unmodifiableCollection(byId.values());
  }

  private void runMultiWaypointHomeCheck() {
    MultiHomeFixer fixer = new MultiHomeFixer(this);
    fixer.run();
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
        LOGGER.error("Couldn't load waypoint {}", e.getKey(), t);
      }
    }

    runMultiWaypointHomeCheck();
    syncResidents();
  }

  protected void save(CompoundTag tag) {
    for (var w : byId.values()) {
      var cTag = BinaryTags.compoundTag();
      w.save(cTag);

      tag.put(w.getId().toString(), cTag);
    }
  }

  private void syncResidents() {
    for (Waypoint waypoint : getWaypoints()) {
      for (UUID uuid : waypoint.getResidents().keySet()) {
        WaypointHomes.setHome(uuid, waypoint);
      }
    }
  }
}