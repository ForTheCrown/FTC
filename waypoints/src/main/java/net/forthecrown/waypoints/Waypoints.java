package net.forthecrown.waypoints;

import com.google.common.base.Strings;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.forthecrown.Loggers;
import net.forthecrown.antigrief.BannedWords;
import net.forthecrown.command.Exceptions;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.PluginJar;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.SerializationHelper;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.WaypointPlatform.FloorPlacer;
import net.forthecrown.waypoints.WaypointPlatform.LoadedPlatform;
import net.forthecrown.waypoints.WaypointScan.Result;
import net.forthecrown.waypoints.type.PlayerWaypointType;
import net.forthecrown.waypoints.type.WaypointType;
import net.forthecrown.waypoints.type.WaypointTypes;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.spongepowered.math.vector.Vector3i;

public final class Waypoints {
  private Waypoints() {}

  /* ------------------------- COLUMN CONSTANTS --------------------------- */

  private static final Logger LOGGER = Loggers.getLogger();

  public static final UUID NIL_UUID = new UUID(0, 0);

  public static final String PLATFORM_OUTLINE_TAG = "waypoint_platform_outline";

  public static final Pattern VALID_NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_$]+");

  public static NamespacedKey EDIT_WAYPOINT_KEY = new NamespacedKey("waypoints", "edit_waypoint");

  public static final Set<Material> NON_REPLACEABLE_FLOOR_MATERIALS = Set.of(
      Material.BARRIER,
      Material.LIGHT,
      Material.BEDROCK,
      Material.END_GATEWAY,
      Material.END_PORTAL_FRAME,
      Material.END_PORTAL,
      Material.JIGSAW,
      Material.STRUCTURE_BLOCK,
      Material.STRUCTURE_VOID,
      Material.DRAGON_EGG,
      Material.NETHER_PORTAL,
      Material.COMMAND_BLOCK,
      Material.CHAIN_COMMAND_BLOCK,
      Material.REPEATING_COMMAND_BLOCK
  );

  /**
   * Gets all invulnerable waypoints within the given bounds in the given world
   */
  public static Set<Waypoint> getInvulnerable(Bounds3i bounds3i, World world) {
    return removeVulnerable(
        WaypointManager.getInstance()
            .getChunkMap()
            .getOverlapping(world, bounds3i)
    );
  }

  /**
   * Gets all invulnerable waypoints at the given position in the given world
   */
  public static Set<Waypoint> getInvulnerable(Vector3i pos, World world) {
    return removeVulnerable(WaypointManager.getInstance().getChunkMap().get(world, pos));
  }

  /**
   * Removes non-invulnerable waypoints from the given set
   */
  private static Set<Waypoint> removeVulnerable(Set<Waypoint> waypoints) {
    Set<Waypoint> result = new ObjectOpenHashSet<>(waypoints);
    result.removeIf(Objects::isNull);
    result.removeIf(waypoint -> !waypoint.get(WaypointProperties.INVULNERABLE));
    return result;
  }

  /**
   * Gets the waypoint the player is currently in
   *
   * @param player The player to find the colliding waypoints of
   * @return The waypoint the player is inside, null, if not inside any waypoints
   */
  public static Waypoint getColliding(Player player) {
    return WaypointManager.getInstance()
        .getChunkMap()
        .getOverlapping(
            player.getWorld(),
            Bounds3i.of(player.getBoundingBox())
        )
        .stream()
        .findAny()
        .orElse(null);
  }

  /**
   * Gets the nearest waypoint to the given user
   *
   * @param user The user to get the nearest waypoint of
   * @return The nearest waypoint to the user, null, if there are no waypoints or the user is in a
   * world with no waypoints
   */
  public static Waypoint getNearest(User user) {
    return WaypointManager.getInstance().getChunkMap().findNearest(user.getLocation()).left();
  }

  /**
   * Tests if the given name is a valid region name.
   * <p>
   * A name is valid if, and only if, it does not contain any banned words, contains no white spaces
   * and does not equal either of the 2 waypoint parsing flags: '-nearest' and '-current'
   *
   * @param name The name to test
   * @return True, if the name is valid, as specified in the above paragraph, false otherwise.
   */
  public static boolean isValidName(String name) {
    return validateWaypointName(name).result().isPresent();
  }

  public static DataResult<String> validateWaypointName(String name) {
    if (Strings.isNullOrEmpty(name)) {
      return Results.error("Empty name");
    }

    Matcher matcher = VALID_NAME_PATTERN.matcher(name);
    if (!matcher.matches()) {
      return Results.error("Name can only contain alphanumeric characters (a-z, 0-9, '_' or '$')");
    }

    WaypointManager manager = WaypointManager.getInstance();
    WaypointConfig config = manager.config();
    int maxLength = config.maxNameLength;

    if (name.length() >= maxLength) {
      return Results.error("Name longer than max length (%s characters max)", maxLength);
    }

    for (String bannedName : config.bannedNames) {
      if (name.equalsIgnoreCase(bannedName)) {
        return Results.error("Banned name");
      }
    }

    Waypoint waypoint = manager.getExtensive(name);

    if (waypoint != null) {
      return Results.error("Name already in use");
    }

    if (BannedWords.contains(name)) {
      return Results.error("Inappropriate name");
    }

    for (var e: manager.getExtensions()) {
      var res = e.isValidName(name);

      if (res.error().isPresent()) {
        return res.map(unit -> name);
      }
    }

    return DataResult.success(name);
  }

  /**
   * Tests if a potential/existing waypoint is in a valid area by testing the blocks within the
   * waypoint boundaries. Optionally, this method will also ensure the waypoint does not overlap
   * with any other waypoints.
   * <p>
   * This method will ensure the waypoint's center column exists, as well as a platform underneath
   * it and that the waypoint's bounds are not obstructed in any way.
   * <p>
   * If <code>testOverlap == true</code> then this method will also ensure that no other waypoints
   * overlap the given one. If any do, an exception is returned.
   * <p>
   * If any of these tests fail, an optional containing the corresponding error will be returned. If
   * all tests are passed however, then an empty optional is returned, indicating the area is
   * valid.
   *
   * @param pos          The position the waypoint will be placed at. Note that this parameter should
   *                     be shifted 1 block upward for region pole waypoints. As the platform
   *                     underneath the region pole is considered as the starting block, instead of
   *                     being under it. To ensure the above is the case use
   *                     {@link PlayerWaypointType#isValid(Waypoint)}, as that performs that
   *                     operation for you
   *
   * @param type         The type to use for validation, this is used to test the column in the
   *                     center of the waypoint.
   *
   * @param w            The world the waypoint is in.
   * @param creationTest {@code true}, when the method is called to validate if a waypoint can
   *                     be created, {@code false}, if it's current state is being tested
   *
   * @return An empty optional if the area is valid, an optional containing a corresponding error
   * message, if the area is invalid
   */
  public static Optional<CommandSyntaxException> isValidWaypointArea(
      Vector3i pos,
      WaypointType type,
      World w,
      boolean creationTest
  ) {
    if (!type.isBuildable()) {
      return Optional.empty();
    }

    var bounds = type.createBounds()
        .move(pos)
        .expand(0, 1, 0, 0, 0, 0)
        .toWorldBounds(w);

    Material[] column = type.getColumn();

    // Test to make sure the area is empty and
    // contains the given type's column
    for (Block b : bounds) {
      // If currently in column position
      if (b.getX() == pos.x() && b.getZ() == pos.z()) {
        int offset = b.getY() - pos.y();

        // Within column bounds
        if (offset < column.length && offset >= 0) {
          Material required = column[offset];

          // If the column block is not the block
          // that is required to be here, then
          // return exception, else, skip this block
          if (b.getType() == required) {
            continue;
          }

          return Optional.of(
              WExceptions.brokenWaypoint(
                  pos.add(0, offset, 0),
                  b.getType(),
                  required
              )
          );
        }
      }

      // If we're on the minY level, which would be the
      // layer right under the waypoint, return an exception,
      // since this layer must be solid, if it is solid,
      // skip block
      if (bounds.minY() == b.getY()) {
        // Ensure that the block can even be replaced, must fail in cases of bedrock,
        // cmd blocks or any other block that cannot be mined by players naturally
        //
        // TODO for the future: Integrate claim checking into this, don't want
        //  players using this to grief somehow lol
        if (creationTest) {
          boolean nonReplaceable = NON_REPLACEABLE_FLOOR_MATERIALS.contains(b.getType());

          if (nonReplaceable) {
            return Optional.of(WExceptions.nonReplaceableFloorBlock(b));
          }

          continue;
        }

        if (b.isSolid()) {
          continue;
        }

        return Optional.of(WExceptions.waypointPlatform());
      }

      // Test if block is empty
      // hardcoded exception for snow lmao
      if (b.getBlockData() instanceof Snow snow) {
        int dif = snow.getMaximumLayers() - snow.getMinimumLayers();
        int half = dif / 2;

        if (snow.getLayers() <= half) {
          continue;
        }

        return Optional.of(WExceptions.snowTooHigh(b));
      } else if (b.isEmpty() || !b.isCollidable() || b.isPassable()) {
        continue;
      }

      return Optional.of(WExceptions.waypointBlockNotEmpty(b));
    }

    if (creationTest) {
      Set<Waypoint> overlapping = WaypointManager.getInstance()
          .getChunkMap()
          .getOverlapping(bounds);

      if (!overlapping.isEmpty()) {
        return Optional.of(
            WExceptions.overlappingWaypoints(overlapping.size())
        );
      }
    }

    return Optional.empty();
  }

  /**
   * Sets a block to be a sign.
   * <p>
   * Any specified {@code consumer} argument doesn't need to call the {@link Sign#update()} method.
   * it's called for you after the consumer is ran.
   *
   * @param block The block to set
   * @param wall {@code true}, to make the sign a wall sign, {@code false} otherwise
   * @param direction Direction for the sign to face, will remain default if {@code null}
   * @param consumer Consumer applied to sign block, sign will be unchanged if {@code null}.
   */
  public static void setSign(
      Block block,
      boolean wall,
      @Nullable BlockFace direction,
      @Nullable Consumer<Sign> consumer
  ) {
    Material material = wall ? Material.OAK_WALL_SIGN : Material.OAK_SIGN;
    BlockData data = material.createBlockData();

    if (direction != null) {
      if (data instanceof org.bukkit.block.data.type.Sign sign) {
        sign.setRotation(direction);
      } else if (data instanceof WallSign wallSign) {
        wallSign.setFacing(direction);
      }
    }

    block.setBlockData(data, false);

    Sign sign = (Sign) block.getState();
    sign.setWaxed(true);

    if (consumer != null) {
      consumer.accept(sign);
    }

    sign.update(false, false);
  }

  /**
   * Attempts to create a waypoint.
   * <p>
   * The given source must be a player. The player must be looking at a valid waypoint top block. If
   * they aren't, an exception is thrown. An exception will also be thrown if the block they are
   * looking at is a guild waypoint block, and they are not in a guild, or do not have permission to
   * move the guild's waypoint, or are not in a chunk owned by their guild.
   * <p>
   * After that, this method will ensure the waypoint's area is valid, see
   * {@link #isValidWaypointArea(Vector3i, WaypointType, World, boolean)}. If that fails, the
   * returned exception is thrown.
   * <p>
   * Then the waypoint is created, if the created waypoint is for a guild, then the guild's waypoint
   * is set as the created waypoint, otherwise, in the case of a player waypoint, the player's home
   * is set to the created waypoint.
   *
   * @param player The player attempting to create the waypoint.
   * @param copy   {@code true}, if the created waypoint will serve as a relocation-copy for
   *               another waypoint
   *
   * @return The created waypoint
   * @throws CommandSyntaxException If the waypoint creation fails at any stage
   */
  public static Waypoint tryCreate(Player player, Block clicked, Waypoint copy)
      throws CommandSyntaxException
  {
    if (clicked == null) {
      throw WExceptions.FACE_WAYPOINT;
    }

    User user = Users.get(player);
    Pair<Block, WaypointType> topAndType = WaypointTypes.findTopAndType(clicked);

    if (!WaypointWorldGuard.canCreateAt(clicked, user)) {
      throw WExceptions.creationDisabled();
    }

    if (topAndType == null) {
      throw WExceptions.invalidWaypointTop(clicked.getType());
    }

    Block b = topAndType.getFirst();
    WaypointType type = topAndType.getSecond();

    var config = WaypointManager.getInstance().config();
    if (config.isDisabledWorld(b.getWorld())) {
      throw WExceptions.WAYPOINTS_WRONG_WORLD;
    }

    Vector3i pos = Vectors.from(b);

    type.onCreate(user, pos, copy != null);

    var existing = WaypointManager.getInstance()
        .getChunkMap()
        .get(b.getWorld(), pos);

    Waypoint waypoint;

    if (existing.isEmpty()) {
      int topOffset = type.getTopOffset();
      pos = pos.sub(0, topOffset, 0);

      // Ensure the area is correct and validate the
      // center block column to ensure it's a proper waypoint
      Optional<CommandSyntaxException> error = isValidWaypointArea(pos, type, b.getWorld(), true);

      if (error.isPresent()) {
        throw error.get();
      }

      if (copy == null) {
        waypoint = makeWaypoint(type, pos, player);
        waypoint.set(WaypointProperties.INVULNERABLE, true);
      } else {
        copy.setPosition(pos, b.getWorld());
        return copy;
      }

    } else {
      throw WExceptions.overlappingWaypoints(existing.size());
    }

    World w = b.getWorld();

    placePlatform(w, waypoint.getPlatform());
    type.onPostCreate(waypoint, user);

    waypoint.update(true);

    return waypoint;
  }

  public static void clearPlatform(World world, Vector3i position) {
    setPlatform(world, position, true);
  }

  public static void placePlatform(World world, Vector3i position) {
    setPlatform(world, position, false);
  }

  private static void setPlatform(World world, Vector3i position, boolean clear) {
    if (world == null || position == null) {
      return;
    }

    var floor = getPlatform();
    FloorPlacer placer = new FloorPlacer(world, position, clear);
    floor.placeAt(placer);
  }

  public static WaypointPlatform getPlatform() {
    Path floorToml = PathUtil.pluginPath("waypoint_platform.toml");
    PluginJar.saveResources("waypoint_platform.toml", floorToml);

    return SerializationHelper.readTomlAsJson(floorToml)
        .flatMap(LoadedPlatform::load)
        .mapError(s -> "Failed to load waypoint_platform.toml: " + s + ", falling back to default")
        .resultOrPartial(LOGGER::error)
        .orElse(WaypointPlatform.DEFAULT);
  }

  /**
   * Tests if the user can move their home waypoint.
   * <p>
   * The cooldown length is determined by {@link WaypointConfig#moveInCooldown}
   *
   * @param user The user to test
   * @throws CommandSyntaxException If they cannot move their waypoint home
   */
  public static void validateMoveInCooldown(User user)
      throws CommandSyntaxException
  {
    long lastMoveIn = user.getTime(TimeField.LAST_MOVEIN);

    // Unset cool downs mean they haven't
    // tried to set their home yet.
    // If movein cooldown disabled or the
    // cooldown length is less than 1
    var config = WaypointManager.getInstance().config();
    Duration duration = config.moveInCooldown;

    if (lastMoveIn == -1 || duration == null || duration.toMillis() < 1) {
      return;
    }

    long remainingCooldown = Time.timeUntil(lastMoveIn + duration.toMillis());

    if (remainingCooldown > 0) {
      throw Exceptions.cooldownEndsIn(remainingCooldown);
    }
  }

  public static Waypoint makeWaypoint(WaypointType type, Vector3i pos, Player source) {
    Location location = source.getLocation();

    if (pos != null) {
      location.setX(pos.x());
      location.setY(pos.y());
      location.setZ(pos.z());
    }

    Waypoint created = makeWaypoint(type, location);

    source.sendMessage(WMessages.createdWaypoint(created.getPosition(), type));
    return created;
  }

  public static Waypoint makeWaypoint(WaypointType type, Location location) {
    Vector3i position = Vectors.intFrom(location);

    Waypoint waypoint = new Waypoint();
    waypoint.setType(type);
    waypoint.setPosition(position, location.getWorld());
    waypoint.setCreationTime(Instant.now());

    WaypointManager.getInstance().addWaypoint(waypoint);

    return waypoint;
  }

  public static void removeIfPossible(Waypoint waypoint) {
    Result scanResult = WaypointScan.scan(waypoint);

    if (!scanResult.isRemovable()) {
      return;
    }

    Loggers.getLogger().info("Removing waypoint {}, reason: {}",
        waypoint.identificationInfo(),
        scanResult.getReason()
    );

    WaypointManager.getInstance().removeWaypoint(waypoint);
  }

  /**
   * Updates the given waypoint's dynmap marker, if and only if, the dynmap plugin is installed.
   *
   * @param waypoint The waypoint to update the marker of
   * @see WaypointWebmaps#updateMarker(Waypoint)
   */
  public static void updateDynmap(Waypoint waypoint) {
    if (!PluginUtil.isEnabled("Dynmap")) {
      return;
    }

    WaypointWebmaps.updateMarker(waypoint);
  }
}