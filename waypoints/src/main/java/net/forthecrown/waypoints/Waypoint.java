package net.forthecrown.waypoints;

import static net.forthecrown.waypoints.Waypoints.PLATFORM_OUTLINE_TAG;
import static net.forthecrown.waypoints.Waypoints.clearPlatform;
import static net.forthecrown.waypoints.Waypoints.placePlatform;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
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
import net.forthecrown.text.TextJoiner;
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
import net.forthecrown.waypoints.type.WaypointType;
import net.forthecrown.waypoints.type.WaypointTypes;
import net.forthecrown.waypoints.util.UuidPersistentDataType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableBoolean;
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

      update(false);
      clearPlatform(getWorld(), getPlatform());

      if (!Objects.equals(world, getWorld())) {
        WaypointWebmaps.removeMarker(this);
      }
    }

    setWorld(world);

    this.position = position;
    this.bounds = createBounds();

    if (hasBeenAdded()) {
      type.onPostMove(this);
      manager.getChunkMap().add(world, bounds, this);

      update(true);
      placePlatform(getWorld(), getPlatform());

      Waypoints.updateDynmap(this);
    }
  }

  private Bounds3i createBounds() {
    var bounds = type.createBounds().move(position);

    int bottomOffset = type.getPlatformOffset();
    if (bottomOffset > 0) {
      bounds = bounds.expand(0, bottomOffset, 0, 0, 0, 0);
    }

    return bounds;
  }

  public void breakColumn() {
    forEachColumnBlock((material, block) -> {
      block.setType(Material.AIR, false);
    });
  }

  public void placeColumn() {
    forEachColumnBlock((material, block) -> {
      block.setType(material, false);
    });
  }

  public void forEachColumnBlock(BiConsumer<Material, Block> consumer) {
    Material[] column = type.getColumn();
    World world = getWorld();

    if (column == null || world == null || !hasBeenAdded()) {
      return;
    }

    Vector3i pos = getPosition().add(0, 1 - type.getPlatformOffset(), 0);

    for (int i = 0; i < column.length; i++) {
      Material mat = column[i];
      Vector3i blockPos = pos.add(0, i, 0);

      consumer.accept(mat, Vectors.getBlock(blockPos, world));
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
      var cloned = propertyValue.clone();
      cloned.setAmount(1);
      return cloned;
    }

    return type.getDisplayItem(this);
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
    String[] helpText =  !state ? null : new String[] { "/help waypoints", "for more info" };

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
    Vector3i top = getAnchor();
    World w = getWorld();

    if (top == null) {
      LOGGER.error("Tried to update nameSign of non-player waypoint {}", this);
      return false;
    }

    if (w == null) {
      LOGGER.error("Cannot set nameSign of waypoint {}: World unloaded", this);
      return false;
    }

    Vector3i pos = top.add(0, 1, 0);
    Block b = Vectors.getBlock(pos, w);

    if (name == null) {
      b.setType(Material.AIR);
    } else {
      String actualName = name.isEmpty() ? "Wilderness" : name;

      Waypoints.setSign(b, false, BlockFace.EAST, sign -> {
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

  public Vector3i getEditSignPosition() {
    var top = getAnchor();
    if (top == null) {
      return null;
    }
    return top.sub(-1, 1, 0);
  }

  public void setEditSign(boolean state) {
    var signPos = getEditSignPosition();
    var world = getWorld();

    if (signPos == null || world == null) {
      return;
    }

    Block block = Vectors.getBlock(signPos, world);

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

    builder.setName(Component.text("[" + effectiveName + "]", getTextColor()));

    builder.addFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ITEM_SPECIFICS);

    BufferedTextWriter writer = TextWriters.buffered();
    configureWriter(writer);
    writer.viewer(viewer);

    writeHover(writer);
    builder.addLoreRaw(writer.getBuffer());

    return builder;
  }

  public void update(boolean state) {
    if (state) {
      updateNameSign();
      updateOutline();
      updateResidentsSign();
    } else {
      setNameSign(null);
      removeOutline();
      removeResidentsSign();
    }

    setEditSign(state);
    setInfoSigns(state);
    setLightBlock(state);
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

    Vector3i boundsSize = getType().createBounds().size();
    var halfSize = boundsSize.toFloat().div(-2);
    Vector3f translation = new Vector3f(halfSize.x(), 0, halfSize.z());
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
        return;
      }

      front.line(3, text("(Right-Click me!)"));

      if (residents.size() == 1) {
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
    if (!isResident(uuid)) {
      return;
    }

    setResident(uuid, System.currentTimeMillis());
  }

  public void setResident(UUID playerId, long time) {
    Objects.requireNonNull(playerId, "Null playerId");

    residents.put(playerId, time);

    if (hasBeenAdded()) {
      updateResidentsSign();

      WaypointHomes.getWaypoint(playerId).ifPresent(w -> {
        // Don't modify if the player's current home if it's already this region
        if (Objects.equals(getId(), w.getId())) {
          return;
        }

        w.removeResident(playerId);
      });

      WaypointHomes.setHome(playerId, this);
    }
  }

  public void removeResident(UUID playerId) {
    Objects.requireNonNull(playerId, "Null playerId");

    residents.removeLong(playerId);

    if (hasBeenAdded()) {
      updateResidentsSign();
      WaypointHomes.setHome(playerId, null);
    }
  }

  public void clearResidents() {
    Set<UUID> residents = new HashSet<>(this.residents.keySet());
    for (UUID resident : residents) {
      removeResident(resident);
    }
  }

  public boolean isResident(UUID uuid) {
    return residents.containsKey(uuid);
  }

  /* ------------------------------ DISPLAY ------------------------------- */

  public void configureWriter(TextWriter writer) {
    writer.setFieldStyle(Style.style(NamedTextColor.GRAY));
    writer.setFieldValueStyle(Style.style(NamedTextColor.YELLOW));
    writer.setFieldSeparator(Component.text(": ", NamedTextColor.GRAY));
  }

  public @Nullable Component displayName() {
    var effectiveName = getEffectiveName();

    if (Strings.isNullOrEmpty(effectiveName)) {
      return null;
    }

    var color = getTextColor();
    var writer = TextWriters.newWriter();

    configureWriter(writer);
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

    Mutable<Boolean> anythingWritten = new MutableBoolean(false);

    type.writeHover(writer, this, anythingWritten);

    if (Worlds.overworld().equals(getWorld())) {
      writer.field("Location", Text.format("{0, vector}", getPosition()));
      anythingWritten.setValue(true);
    }

    if (!get(WaypointProperties.HIDE_RESIDENTS)) {
      int residents = getResidents().size();

      if (residents > 1) {
        writer.field("Residents", Text.formatNumber(residents));
        anythingWritten.setValue(true);
      } else if (residents == 1) {
        UUID resident = this.residents.keySet().iterator().next();
        writer.formattedField("Resident", "{0, user}", resident);
        anythingWritten.setValue(true);
      }
    }

    List<String> aliases = get(WaypointProperties.ALIASES);

    if (aliases != null && !aliases.isEmpty()) {
      Component joined = TextJoiner.newJoiner()
          .setDelimiter(text(", ", NamedTextColor.GRAY))
          .add(aliases.stream().map(string -> text("'" + string + "'")))
          .asComponent();

      writer.field("Name Aliases", joined);
      anythingWritten.setValue(true);
    }

    if (anythingWritten.getValue()) {
      writer.newLine();
      writer.newLine();
    }

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
    return type.getNameColor(this);
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
    this.bounds = createBounds();

    if (tag.containsKey(TAG_WORLD)) {
      var key = TagUtil.readKey(tag.get(TAG_WORLD));

      var world = Bukkit.getWorld(key);
      setWorld(world);
    }

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