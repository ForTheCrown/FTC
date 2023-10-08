package net.forthecrown.waypoints;

import com.google.common.base.Strings;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongObjectPair;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.Worlds;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.LongTag;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.TagOps;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.type.WaypointType;
import net.forthecrown.waypoints.type.WaypointTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.math.vector.Vector3i;

/**
 * A waypoint is a teleport-enabling area where users can teleport to other waypoints which are
 * placed arbitrarily throughout the server.
 */
public class Waypoint {

  private static final Logger LOGGER = Loggers.getLogger();

  static final String
      TAG_POS = "position",
      TAG_WORLD = "world",
      TAG_TYPE = "type",
      TAG_PROPERTIES = "properties",
      TAG_RESIDENTS = "residents",
      TAG_LAST_VALID = "lastValid";

  /**
   * The Waypoint's randomly generated UUID
   */
  @Getter
  private transient final UUID id;

  /**
   * The position of the waypoint.
   * <p>
   * Note: This position will be saved, but the {@link #bounds} field won't, this is because the
   * bounds is created by the waypoint's type
   */
  @Getter
  private Vector3i position = Vector3i.ZERO;

  /**
   * The Bounding box of this waypoint, being inside it means a player can use it.
   * <p>
   * If this waypoint is invulnerable (determined by the {@link WaypointProperties#INVULNERABLE}
   * property) Then any blocks within these bounds will not be destroy-able
   */
  @Getter
  private Bounds3i bounds = Bounds3i.EMPTY;

  /**
   * This Waypoint's world.
   * <p>
   * Reference used here in a similar fashion to {@link org.bukkit.Location}. This is because the
   * underlying world this waypoint is in may be unloaded, deleted or otherwise removed
   */
  private Reference<World> world;

  /**
   * This waypoint's type.
   * <p>
   * If the type is that of a region pole, this waypoint will generate region poles wherever its
   * placed. These poles are also removed when the waypoint is moved or deleted
   */
  @Setter
  @Getter
  private WaypointType type;

  /**
   * Property values
   */
  private Object[] properties = ArrayUtils.EMPTY_OBJECT_ARRAY;

  private CompoundTag unknownProperties;

  /**
   * Map of User UUID to Pair(Inviter UUID, Invite sent timestamp)
   */
  @Getter
  private final Map<UUID, LongObjectPair<UUID>> invites = new Object2ObjectOpenHashMap<>();

  /**
   * Map of Users' UUID to the date they set this waypoint as their home
   */
  @Getter
  private final Object2LongMap<UUID> residents = new Object2LongOpenHashMap<>();

  /**
   * Last time this waypoint was 'valid'
   * <p>
   * Valid means the waypoint's 'pole' exists and all other requirements specified in
   * {@link Waypoints#isValidWaypointArea(Vector3i, WaypointType, World, boolean)} are filled.
   * This field is used by the day change listener in {@link WaypointManager} to track when this
   * pole was last valid and delete poles that are not valid for extended periods of time
   */
  @Getter
  @Setter
  private long lastValidTime = -1;

  @Setter
  private String effectiveName;

  /**
   * Manager this waypoint has been added to
   */
  WaypointManager manager;

  /* --------------------------- CONSTRUCTORS ---------------------------- */

  public Waypoint() {
    this(UUID.randomUUID());
  }

  public Waypoint(UUID id) {
    Objects.requireNonNull(id);

    this.id = id;
    this.effectiveName = id.toString();
  }

  /* ------------------------------ METHODS ------------------------------- */

  /**
   * Tests if this waypoint has been added to the manager
   *
   * @return True, if this waypoint has been added to the waypoint manager, false otherwise
   */
  public boolean hasBeenAdded() {
    return manager != null;
  }

  /**
   * Tests if this waypoint's world is loaded
   *
   * @return True, if the world the waypoint is in is loaded, false otherwise
   */
  public boolean isWorldLoaded() {
    return getWorld() != null;
  }

  /**
   * Sets the position of this waypoint
   *
   * @param position The waypoint's block position
   * @param world    The world the waypoint will be moved to
   */
  public void setPosition(@NotNull Vector3i position, @NotNull World world) {
    // Ensure no nulls
    Objects.requireNonNull(position, "Position was null");
    Objects.requireNonNull(world, "World cannot be null");

    // If this is already our location, don't do anything
    if (Objects.equals(position, getPosition()) && Objects.equals(world, getWorld())) {
      return;
    }

    // If not added, most likely being deserialized, so we don't
    // need to update any actual values
    if (hasBeenAdded() && getWorld() != null) {
      type.onPreMove(this, position, world);
      manager.getChunkMap().remove(getWorld(), this);
      Waypoints.updateDynmap(this);
    }

    setWorld(world);
    this.position = position;
    this.bounds = type.createBounds().move(position);

    if (hasBeenAdded()) {
      type.onPostMove(this);
      manager.getChunkMap().add(world, bounds, this);
    }
  }

  /**
   * Gets the world this waypoint is in
   *
   * @return The waypoint's world, null
   */
  public @Nullable World getWorld() {
    return world == null ? null : world.get();
  }

  private void setWorld(@Nullable World world) {
    if (world == null) {
      this.world = null;
      return;
    }

    this.world = new WeakReference<>(world);
  }

  public String getMarkerId() {
    return "waypoint." + getId();
  }

  public String identificationInfo() {
    return String.format("(id=%s, name='%s', pos=%s, world=%s)",
        getId(),
        get(WaypointProperties.NAME),
        getPosition(),
        getWorld() == null ? null : getWorld().getName()
    );
  }

  /* ---------------------------- PROPERTIES ----------------------------- */

  @SuppressWarnings("unchecked")
  public <T> T get(WaypointProperty<T> property) {
    if (!has(property)) {
      return property.getDefaultValue();
    }

    return (T) properties[property.getId()];
  }

  public <T> boolean has(WaypointProperty<T> property) {
    int index = property.getId();

    if (index >= properties.length) {
      return false;
    }

    var value = properties[index];

    if (value == null) {
      return false;
    }

    return !value.equals(property.getDefaultValue());
  }

  public <T> boolean set(@NotNull WaypointProperty<T> property, @Nullable T value) {
    T current = get(property);

    // If current value == given value, don't change anything
    if ((current == null && value == null) || Objects.equals(current, value)) {
      return false;
    }

    int index = property.getId();

    // If we should unset the value
    if (value == null || Objects.equals(value, property.getDefaultValue())) {
      // If the property value array isn't large enough
      // for the property we want to unset
      if (index >= properties.length) {
        return false;
      }

      value = null;
      properties[index] = null;
    } else {
      properties = ObjectArrays.ensureCapacity(properties, index + 1);
      properties[index] = value;
    }

    // If this method is called before the waypoint is added to
    // the manager, it's most likely because it's being deserialized,
    // during deserialization nothing should be updated as there's
    // no need to update any world data or such
    if (hasBeenAdded()) {
      property.onValueUpdate(this, current, value);
    }

    return true;
  }

  public boolean hasProperties() {
    return ArrayIterator.unmodifiable(properties).hasNext();
  }

  /* ----------------------------- INVITES ------------------------------ */

  public boolean hasValidInvite(UUID uuid) {
    var pair = invites.get(uuid);

    if (pair == null) {
      return false;
    }

    long validInviteTime = WaypointManager.getInstance().config().validInviteTime.toMillis();

    if (Time.isPast(pair.firstLong() + validInviteTime)) {
      invites.remove(uuid);
      return false;
    }

    return true;
  }

  public void invite(UUID inviter, UUID target) {
    LongObjectPair<UUID> pair = LongObjectPair.of(
        System.currentTimeMillis(), inviter
    );

    invites.put(target, pair);
  }

  /* ----------------------------- RESIDENTS ------------------------------ */

  public void addResident(UUID uuid) {
    setResident(uuid, System.currentTimeMillis());
  }

  public void setResident(UUID uuid, long time) {
    if (isResident(uuid)) {
      return;
    }

    residents.put(uuid, time);
  }

  public void removeResident(UUID uuid) {
    residents.removeLong(uuid);
  }

  public boolean isResident(UUID uuid) {
    return residents.containsKey(uuid);
  }

  /* ------------------------------ DISPLAY ------------------------------- */

  public @Nullable Component displayName() {
    var effectiveName = getEffectiveName();

    if (Strings.isNullOrEmpty(effectiveName)) {
      return null;
    }

    var color = getTextColor();
    var writer = TextWriters.newWriter();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));

    writeHover(writer);

    return Text.format("[{0}]", color, effectiveName)
        .hoverEvent(writer.asComponent())
        .clickEvent(ClickEvent.suggestCommand("/visit " + effectiveName));
  }

  private void writeHover(TextWriter writer) {
    writer.formattedLine("{0} Waypoint",
        writer.getFieldStyle(),
        getType().getDisplayName()
    );

    if (!Objects.equals(id.toString(), effectiveName)) {
      writer.field("Owner", effectiveName);
    }

    if (Worlds.overworld().equals(getWorld())) {
      writer.field("Location", Text.format("{0, vector}", getPosition()));
    }

    int residents = getResidents().size();
    if (residents > 0) {
      writer.field("Residents", Text.formatNumber(residents));
    }
  }

  private TextColor getTextColor() {
    return type.getNameColor();
  }

  /**
   * Gets the effective name of the waypoint.
   * <p>
   * What effective in this case means, is the name that should be displayed on the waypoint. If the
   * given waypoint is owned by a guild, but has no custom name set, then this will return the
   * guild's name. If a name is set, then it is returned always, even if the waypoint has a name,
   *
   * @return The gotten name, may be null
   */
  public @Nullable String getEffectiveName() {
    String fromProperty = get(WaypointProperties.NAME);

    if (!Strings.isNullOrEmpty(fromProperty)) {
      return fromProperty;
    }

    String fromType = type.getEffectiveName(this);
    if (!Strings.isNullOrEmpty(fromType)) {
      return fromType;
    }

    return effectiveName;
  }

  /* --------------------------- SERIALIZATION ---------------------------- */

  public void save(CompoundTag tag) {
    WaypointTypes.REGISTRY.writeTag(type)
        .ifPresent(tag1 -> tag.put(TAG_TYPE, tag1));

    tag.put(TAG_POS, Vectors.writeTag(position));

    var world = getWorld();
    if (world != null) {
      tag.put(TAG_WORLD, TagUtil.writeKey(world.getKey()));
    } else {
      LOGGER.warn("Null world in waypoint {}", getId());
    }

    if (lastValidTime != -1) {
      tag.putLong(TAG_LAST_VALID, lastValidTime);
    }

    if (hasProperties()) {
      CompoundTag propTag = BinaryTags.compoundTag();
      ArrayIterator<Object> it = ArrayIterator.unmodifiable(properties);

      while (it.hasNext()) {
        int id = it.nextIndex();
        var next = it.next();

        WaypointProperties.REGISTRY.get(id)
            .ifPresentOrElse(property -> {
              if (next == null || Objects.equals(property.getDefaultValue(), next)) {
                return;
              }

              WaypointProperty<Object> prop = property;
              Codec<Object> codec = prop.getCodec();
              BinaryTag pTag = codec.encodeStart(TagOps.OPS, next).getOrThrow(false, s -> {});

              propTag.put(property.getName(), pTag);
            }, () -> {
              LOGGER.warn("Unknown property at index {}", id);
            });
      }

      if (unknownProperties != null && !unknownProperties.isEmpty()) {
        propTag.putAll(unknownProperties);
      }

      tag.put(TAG_PROPERTIES, propTag);
    }

    if (!residents.isEmpty()) {
      CompoundTag rTag = BinaryTags.compoundTag();
      residents.forEach((uuid, aLong) -> {
        rTag.putLong(uuid.toString(), aLong);
      });

      tag.put(TAG_RESIDENTS, rTag);
    }
  }

  @SuppressWarnings("unchecked")
  public void load(CompoundTag tag) {
    var opt = WaypointTypes.REGISTRY.readTag(tag.get(TAG_TYPE));

    if (opt.isEmpty()) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.error("Waypoint {} has unknown type '{}', defaulting to player type",
            id, tag.get(TAG_TYPE)
        );
        type = WaypointTypes.PLAYER;
      } else {
        // Will just throw an exception
        WaypointTypes.REGISTRY.readTagOrThrow(tag.get(TAG_TYPE));
      }
    } else {
      type = opt.get();
    }

    this.position = Vectors.read3i(tag.get(TAG_POS));

    if (tag.containsKey(TAG_WORLD)) {
      var key = TagUtil.readKey(tag.get(TAG_WORLD));

      var world = Bukkit.getWorld(key);
      setWorld(world);
    }

    this.bounds = type.createBounds().move(position);

    if (tag.containsKey(TAG_LAST_VALID)) {
      lastValidTime = tag.getLong(TAG_LAST_VALID);
    } else {
      lastValidTime = -1;
    }

    if (tag.containsKey(TAG_PROPERTIES)) {
      CompoundTag propertyTag = tag.getCompound(TAG_PROPERTIES);

      for (var e : propertyTag.entrySet()) {
        WaypointProperties.REGISTRY.get(e.getKey()).ifPresentOrElse(property -> {
          WaypointProperty<Object> prop = property;

          prop.getCodec()
              .decode(TagOps.OPS, e.getValue())
              .map(Pair::getFirst)
              .resultOrPartial(LOGGER::warn)
              .ifPresent(o -> set(property, o));
        }, () -> {
          LOGGER.warn("Unknown property '{}', adding to unknowns list", e.getKey());

          if (unknownProperties == null) {
            unknownProperties = BinaryTags.compoundTag();
          }
          unknownProperties.put(e.getKey(), e.getValue());
        });
      }
    } else {
      properties = ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    if (tag.containsKey(TAG_RESIDENTS)) {
      CompoundTag rTag = tag.getCompound(TAG_RESIDENTS);
      rTag.forEach((s, tag1) -> {
        UUID uuid = UUID.fromString(s);
        long time = ((LongTag) tag1).longValue();

        setResident(uuid, time);
      });
    } else {
      residents.clear();
    }
  }

  /* ------------------------- OBJECT OVERRIDES -------------------------- */

  @Override
  public String toString() {
    return identificationInfo();
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Waypoint w)) {
      return false;
    }

    return w.getId().equals(id);
  }
}