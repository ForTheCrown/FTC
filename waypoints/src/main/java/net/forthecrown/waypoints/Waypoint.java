package net.forthecrown.waypoints;

import static net.forthecrown.waypoints.Waypoints.PLATFORM_OUTLINE_TAG;
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;

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
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.forthecrown.Loggers;
import net.forthecrown.Worlds;
import net.forthecrown.nbt.BinaryTag;
import net.forthecrown.nbt.BinaryTags;
import net.forthecrown.nbt.CompoundTag;
import net.forthecrown.nbt.LongTag;
import net.forthecrown.nbt.TagTypes;
import net.forthecrown.text.BufferedTextWriter;
import net.forthecrown.text.PlayerMessage;
import net.forthecrown.text.Text;
import net.forthecrown.text.TextWriter;
import net.forthecrown.text.TextWriters;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.ArrayIterator;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.inventory.ItemBuilder;
import net.forthecrown.utils.inventory.ItemStacks;
import net.forthecrown.utils.io.TagOps;
import net.forthecrown.utils.io.TagUtil;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Direction;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.type.PlayerWaypointType;
import net.forthecrown.waypoints.type.WaypointType;
import net.forthecrown.waypoints.type.WaypointTypes;
import net.forthecrown.waypoints.util.UuidPersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Light;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.spongepowered.math.vector.Vector3i;

/**
 * A waypoint is a teleport-enabling area where users can teleport to other waypoints which are
 * placed arbitrarily throughout the server.
 */
public class Waypoint {

  private static final Logger LOGGER = Loggers.getLogger();

  static final String TAG_POS = "position";
  static final String TAG_WORLD = "world";
  static final String TAG_TYPE = "type";
  static final String TAG_PROPERTIES = "properties";
  static final String TAG_RESIDENTS = "residents";
  static final String TAG_LAST_VALID = "lastValid";
  static final String TAG_CREATION_TIME = "creation_time";
  static final String TAG_DESCRIPTION = "description";

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

  @Getter @Setter
  private Instant creationTime;

  @Getter @Setter
  private PlayerMessage description;

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
      removeOutline();

      if (!Objects.equals(world, getWorld())) {
        WaypointWebmaps.removeMarker(this);
      }
    }

    setWorld(world);
    this.position = position;
    this.bounds = type.createBounds().move(position);

    if (hasBeenAdded()) {
      type.onPostMove(this);
      manager.getChunkMap().add(world, bounds, this);
      updateOutline();

      Waypoints.updateDynmap(this);
    }
  }

  /**
   * Gets the position of the Waypoint's top block, also known as the waypoint's anchor
   * <p>
   * This method will return {@code null} if the waypoint's type is 'admin' or any other waypoint
   * type that doesn't require a physical structure in order to exist.
   * <p>
   * More specifically, if {@link WaypointType#getTopOffset()} returns less than 0, this will
   * return {@code null}.
   *
   * @return Waypoint's top block position, or {@code null}, if the waypoint doesn't have a 'top'
   *         block.
   */
  public @Nullable Vector3i getAnchor() {
    int offset = type.getTopOffset();

    if (offset < 0) {
      return null;
    }

    return getPosition().add(0, offset, 0);
  }

  /**
   * Gets the center position of the waypoint's platform
   * <p>
   * This method will return {@code null} if the waypoint's type is 'admin' or any other waypoint
   * type that doesn't require a physical structure in order to exist.
   * <p>
   * More specifically, if {@link WaypointType#getPlatformOffset()} returns less than 0, this will
   * return {@code null}.
   *
   * @return Platform center position, or {@code null}, if the waypoint doesn't have a platform
   */
  public @Nullable Vector3i getPlatform() {
    int offset = type.getPlatformOffset();

    if (offset < 0) {
      return null;
    }

    return getPosition().sub(0, offset, 0);
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
    return String.format("Waypoint(id=%s, name='%s', pos=%s, world=%s)",
        getId(),
        get(WaypointProperties.NAME),
        getPosition(),
        getWorld() == null ? null : getWorld().getName()
    );
  }

  public void onVisit() {
    incrementProperty(WaypointProperties.VISITS_DAILY);
    incrementProperty(WaypointProperties.VISITS_MONTHLY);
    incrementProperty(WaypointProperties.VISITS_TOTAL);
  }

  private void incrementProperty(WaypointProperty<Integer> prop) {
    Integer val = get(prop);
    set(prop, val == null ? 1 : val + 1);
  }

  /**
   * Tests if the specified {@code user} is allowed to edit/rename/move/delete this waypoint
   * <p>
   * Other than the inbuilt admin permission test, this will simply defer the method's result to
   * {@link WaypointType#canEdit(User, Waypoint)}
   *
   * @param user User to test
   * @return {@code true}, if allowed to edit, {@code false} otherwise
   */
  public boolean canEdit(User user) {
    if (user.hasPermission(WPermissions.WAYPOINTS_ADMIN)) {
      return true;
    }

    return type.canEdit(user, this);
  }

  /**
   * Gets the waypoint's display item.
   * <p>
   * First checks if the {@link WaypointProperties#DISPLAY_ITEM} property is set, if it is, its
   * value is returned. If not, then {@link WaypointType#getDisplayItem(Waypoint)} is called
   *
   * @return Waypoint's display item, or {@code null}, if none was found
   */
  public ItemStack getDisplayItem() {
    var propertyValue = get(WaypointProperties.DISPLAY_ITEM);
    if (ItemStacks.notEmpty(propertyValue)) {
      return propertyValue.clone();
    }

    return type.getDisplayItem(this);
  }

  /**
   * Copies data from the specified {@code other} waypoint to this one.
   * <p>
   * Copied data includes: Residents and properties
   *
   * @param other Waypoint to copy from
   */
  public void copyFrom(@NotNull Waypoint other) {
    Objects.requireNonNull(other, "Null other");

    Object2LongMap<UUID> residentsCopy = new Object2LongOpenHashMap<>(other.getResidents());
    residentsCopy.forEach((uuid, timestamp) -> {
      User user = Users.get(uuid);
      user.set(WaypointPrefs.HOME_PROPERTY, getId());

      // Call this method even though the HOME_PROPERTY's callback
      // calls addResident to keep track of when the user set their
      // home here accurately
      this.residents.put(uuid, timestamp);
    });

    unknownProperties = other.unknownProperties == null ? null : other.unknownProperties.copy();
    properties = other.properties;
    description = other.description;
    creationTime = other.creationTime;
  }

  /**
   * Sets the state of the info signs around the waypoint's anchor block.
   * @param state {@code true} to create the signs, {@code false} to remove them.
   */
  public void setInfoSigns(boolean state) {
    var top = getAnchor();
    var world = getWorld();

    if (top == null || world == null) {
      return;
    }

    String[] visitInfo = !state ? null : new String[] { "/visit <region>", "to teleport." };
    String[] helpText =  !state ? null : new String[] { "/polehelp", "for more info" };

    setInfoSign(world, top, Direction.NORTH, visitInfo);
    setInfoSign(world, top, Direction.SOUTH, visitInfo);
    setInfoSign(world, top,  Direction.WEST,  helpText);
  }

  private void setInfoSign(World w, Vector3i pos, Direction dir, String[] text) {
    BlockFace face = dir.asBlockFace();
    Vector3i blockPos = pos.add(dir.getMod());

    Block block = Vectors.getBlock(blockPos, w);

    if (text == null) {
      block.setType(Material.AIR, false);
      return;
    }

    Waypoints.setSign(block, true, face, sign -> {
      SignSide front = sign.getSide(Side.FRONT);

      front.setGlowingText(true);
      front.setColor(DyeColor.GRAY);

      front.line(0, empty());
      front.line(1, text(text[0]));
      front.line(2, text(text[1]));
      front.line(3, empty());
    });
  }

  /**
   * Update's the waypoint's name sign to {@link #getEffectiveName()}. If
   * {@link #getEffectiveName()} returns {@code null} or an empty string, the sign is changed to
   * "Wilderness"
   */
  public void updateNameSign() {
    setNameSign(Strings.nullToEmpty(getEffectiveName()));
  }

  /**
   * Sets the waypoint's name sign.
   * <p>
   * If the specified {@code name} is {@code null}, the sign is removed. It's an empty string, the
   * name sign's text will be changed to "Wilderness", otherwise the sign will show the specified
   * name
   *
   * @param name The name to set the sign to, if null, the sign is removed
   * @return {@code true}, if the name sign was successfully changed, {@code false} otherwise
   */
  public boolean setNameSign(String name) {
    if (!(type instanceof PlayerWaypointType)) {
      LOGGER.error("Tried to update name sign on non-player waypoint! waypoint: {}", this);
      return false;
    }

    Vector3i pos = getAnchor().add(0, 1, 0);
    World w = getWorld();

    if (w == null) {
      LOGGER.error("Cannot set nameSign of waypoint {}: World unloaded", this);
      return false;
    }

    Block b = Vectors.getBlock(pos, w);

    if (name == null) {
      b.setType(Material.AIR);
    } else {
      String actualName = name.isEmpty() ? "Wilderness" : name;

      Waypoints.setSign(b, false, null, sign -> {
        setNameOnSide(sign.getSide(Side.FRONT), actualName);
        setNameOnSide(sign.getSide(Side.BACK), actualName);
      });
    }

    return true;
  }

  private static void setNameOnSide(SignSide side, String name) {
    side.setGlowingText(true);
    side.setColor(DyeColor.GRAY);

    side.line(0, empty());
    side.line(1, text(name));
    side.line(2, text("Waypoint"));
    side.line(3, empty());
  }

  /**
   * Sets the state of the light block above the waypoint. Will fail if {@link #getAnchor()} returns
   * {@code null}
   * @param state {@code true} to ensure the light block exists, {@code false} to remove it
   */
  public void setLightBlock(boolean state) {
    var top = getAnchor();
    var world = getWorld();

    if (top == null || world == null) {
      return;
    }

    Block block = Vectors.getBlock(top.add(0, 2, 0), world);

    if (state) {
      Light light = (Light) Material.LIGHT.createBlockData();
      light.setLevel(light.getMaximumLevel());
      block.setBlockData(light, false);
    } else if (block.getType() == Material.LIGHT) {
      block.setType(Material.AIR, false);
    }
  }

  public void setEditSign(boolean state) {
    var top = getAnchor();
    var world = getWorld();

    if (top == null || world == null) {
      return;
    }

    Block block = Vectors.getBlock(top.sub(-1, 1, 0), world);

    if (!state) {
      block.setType(Material.AIR, false);
      return;
    }

    Waypoints.setSign(block, true, BlockFace.EAST, sign -> {
      var pdc = sign.getPersistentDataContainer();
      pdc.set(Waypoints.EDIT_WAYPOINT_KEY, UuidPersistentDataType.INSTANCE, getId());

      SignSide front = sign.getSide(Side.FRONT);

      front.setGlowingText(true);
      front.setColor(DyeColor.GRAY);

      ClickEvent clickEvent = ClickEvent.runCommand("/waypointgui " + getId());

      front.line(0, empty());
      front.line(1, text("Right-Click to"));
      front.line(2, text("edit waypoint").clickEvent(clickEvent));
      front.line(3, empty());
    });
  }

  public ItemBuilder<?> createDisplayItem(User viewer) {
    ItemStack baseItem = getDisplayItem();
    String effectiveName = getEffectiveName();

    if (Strings.isNullOrEmpty(effectiveName)) {
      effectiveName = "Waypoint";
    }

    ItemBuilder<?> builder = ItemStacks.toBuilder(
        ItemStacks.isEmpty(baseItem) ? new ItemStack(Material.NAME_TAG) : baseItem
    );

    builder.setName(
        Component.text("[" + effectiveName + "]")
            .color(getType().getNameColor())
    );

    builder.addFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ITEM_SPECIFICS);

    BufferedTextWriter writer = TextWriters.buffered();
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
    writer.setFieldValueStyle(Style.style(NamedTextColor.YELLOW));
    writer.setFieldSeparator(Component.text(": ", NamedTextColor.GRAY));
    writer.viewer(viewer);

    writeHover(writer);
    builder.addLoreRaw(writer.getBuffer());

    return builder;
  }

  /* ------------------------- PLATFORM OUTLINE -------------------------- */

  public void updateOutline() {
    var opt = getOutline().or(this::createDisplay);

    if (opt.isEmpty()) {
      return;
    }

    BlockDisplay display = opt.get();

    if (!display.getScoreboardTags().contains(PLATFORM_OUTLINE_TAG)) {
      display.addScoreboardTag(PLATFORM_OUTLINE_TAG);
    }

    display.setBlock(Material.STONE.createBlockData());
    display.setShadowRadius(0f);
    display.setShadowStrength(0f);
    display.setViewRange(0.1f);
    display.setGlowing(true);

    Vector3i boundsSize = bounds.size();
    var halfSize = boundsSize.toFloat().div(-2);
    Vector3f translation = new Vector3f(halfSize.x(), 0, halfSize.y());
    Vector3f scale = new Vector3f(boundsSize.x(), 0.01f, boundsSize.z());

    // Amount the scale and translation are modified to prevent
    // texture Z fighting on the sides
    final float texClip = 0.01f;

    translation.add(texClip / 2, 0, texClip / 2);
    scale.sub(texClip, 0, texClip);

    display.setTransformation(
        new Transformation(translation, new AxisAngle4f(), scale, new AxisAngle4f())
    );
  }

  private Location getOutlineSpawn() {
    var pos = getPlatform();
    var world = getWorld();

    if (world == null || pos == null) {
      return null;
    }

    Location spawnLocation = new Location(world, pos.x(), pos.y(), pos.z());
    spawnLocation.add(0.5, 0.98, 0.5);

    return spawnLocation;
  }

  private Optional<BlockDisplay> createDisplay() {
    var spawn = getOutlineSpawn();

    if (spawn == null) {
      return Optional.empty();
    }

    var spawned = spawn.getWorld().spawn(spawn, BlockDisplay.class);
    return Optional.of(spawned);
  }

  public void removeOutline() {
    getOutline().ifPresent(Entity::remove);
  }

  public Optional<BlockDisplay> getOutline() {
    Location spawnLocation = getOutlineSpawn();

    if (spawnLocation == null) {
      return Optional.empty();
    }

    var chunk = spawnLocation.getChunk();

    // calling getEntities() forces the chunk to load all entities it has,
    // should therefor avoid the issue of entities becoming unreachable
    // outside loaded chunks
    if (!chunk.isEntitiesLoaded()) {
      chunk.getEntities();
    }

    Collection<BlockDisplay> collection = spawnLocation.getNearbyEntitiesByType(
        BlockDisplay.class,
        0.5,
        display -> {
          return display.getScoreboardTags().contains(PLATFORM_OUTLINE_TAG);
        }
    );

    if (collection.isEmpty()) {
      return Optional.empty();
    }

    if (collection.size() > 1) {
      LOGGER.warn("More than 1 platform outlines found at {}", spawnLocation);
    }

    return Optional.of(collection.iterator().next());
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

  public Vector3i getResidentsSign() {
    Vector3i top = getAnchor();

    if (top == null) {
      return null;
    }

    return top.add(1, 0, 0);
  }

  public void removeResidentsSign() {
    var signPos = getResidentsSign();
    var world = getWorld();

    if (signPos == null || world == null) {
      return;
    }

    Block block = Vectors.getBlock(signPos, world);
    block.setType(Material.AIR, false);
  }

  public void updateResidentsSign() {
    var signPos = getResidentsSign();
    var world = getWorld();

    if (signPos == null || world == null) {
      return;
    }

    Block block = Vectors.getBlock(signPos, world);

    Waypoints.setSign(block, true, BlockFace.EAST, sign -> {
      SignSide front = sign.getSide(Side.FRONT);

      front.setGlowingText(true);
      front.setColor(DyeColor.GRAY);

      front.line(0,
          empty().clickEvent(ClickEvent.runCommand("/waypointgui residents " + getId()))
      );

      if (get(WaypointProperties.HIDE_RESIDENTS)) {
        front.line(1, text("Residents:"));
        front.line(2, text("¯\\_(ツ)_/¯"));
      } else if (residents.size() == 1) {
        User resident = Users.get(residents.keySet().iterator().next());
        front.line(1, text("Resident:"));
        front.line(2, resident.nickOrName());
      } else {
        front.line(1, text("Residents:"));

        if (residents.isEmpty()) {
          front.line(2, text("No one :("));
        } else {
          front.line(2, text(residents.size()));
        }
      }
    });
  }

  public void addResident(UUID uuid) {
    setResident(uuid, System.currentTimeMillis());

    if (hasBeenAdded()) {
      updateResidentsSign();
    }
  }

  public void setResident(UUID uuid, long time) {
    if (isResident(uuid)) {
      return;
    }

    residents.put(uuid, time);
  }

  public void removeResident(UUID uuid) {
    residents.removeLong(uuid);

    if (hasBeenAdded()) {
      updateResidentsSign();
    }
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

  public void writeHover(TextWriter writer) {
    if (description != null) {
      writer.line(description);

      writer.newLine();
      writer.newLine();

      writer.formattedField("Type",
          "{0} Waypoint",
          writer.getFieldStyle(),
          getType().getDisplayName()
      );
    } else {
      writer.formattedLine("{0} Waypoint",
          writer.getFieldStyle(),
          getType().getDisplayName()
      );

      writer.newLine();
      writer.newLine();
    }

    type.writeHover(writer, this);

    if (Worlds.overworld().equals(getWorld())) {
      writer.field("Location", Text.format("{0, vector}", getPosition()));
    }

    if (!get(WaypointProperties.HIDE_RESIDENTS)) {
      int residents = getResidents().size();

      if (residents > 1) {
        writer.field("Residents", Text.formatNumber(residents));
      } else if (residents == 1) {
        UUID resident = this.residents.keySet().iterator().next();
        writer.formattedField("Resident", "{0, user}", resident);
      }
    }

    writer.newLine();
    writer.newLine();
    writer.field("Stats");

    writer.field("Visits today", get(WaypointProperties.VISITS_DAILY));
    writer.field("Monthly visits", get(WaypointProperties.VISITS_MONTHLY));
    writer.field("Total visits", get(WaypointProperties.VISITS_TOTAL));

    if (creationTime == null) {
      writer.field("Creation date", "¯\\_(ツ)_/¯");
    } else {
      writer.field("Creation date", Text.formatDate(creationTime));
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

    if (Objects.equals(id.toString(), effectiveName)) {
      return null;
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

    if (creationTime != null) {
      tag.putLong(TAG_CREATION_TIME, creationTime.toEpochMilli());
    }

    if (description != null) {
      description.save(TagOps.OPS)
          .mapError(s -> "Failed to save description for waypoint " + this + ": " + s)
          .resultOrPartial(LOGGER::error)
          .ifPresent(binaryTag -> tag.put(TAG_DESCRIPTION, binaryTag));
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

    if (tag.contains(TAG_CREATION_TIME, TagTypes.longType())) {
      long epochMillis = tag.getLong(TAG_CREATION_TIME);
      creationTime = Instant.ofEpochMilli(epochMillis);
    } else {
      creationTime = null;
    }

    if (tag.contains(TAG_DESCRIPTION)) {
      PlayerMessage.CODEC.parse(TagOps.OPS, tag.get(TAG_DESCRIPTION))
          .mapError(s -> "Failed to load description for waypoint " + this + ": " + s)
          .resultOrPartial(LOGGER::error)
          .ifPresentOrElse(
              this::setDescription,
              () -> setDescription(null)
          );
    } else {
      description = null;
    }

    if (tag.containsKey(TAG_PROPERTIES)) {
      CompoundTag propertyTag = tag.getCompound(TAG_PROPERTIES);

      for (var e : propertyTag.entrySet()) {
        String renamedKey = WaypointProperties.RENAMES.getOrDefault(e.getKey(), e.getKey());

        WaypointProperties.REGISTRY.get(renamedKey).ifPresentOrElse(property -> {
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
      unknownProperties = null;
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