package net.forthecrown.waypoints;

import static net.kyori.adventure.text.Component.text;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.forthecrown.Loggers;
import net.forthecrown.antigrief.BannedWords;
import net.forthecrown.command.Exceptions;
import net.forthecrown.grenadier.CommandSource;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.FunctionInfo;
import net.forthecrown.structure.StructurePlaceConfig;
import net.forthecrown.structure.Structures;
import net.forthecrown.structure.buffer.ImmediateBlockBuffer;
import net.forthecrown.text.Text;
import net.forthecrown.user.TimeField;
import net.forthecrown.user.User;
import net.forthecrown.user.Users;
import net.forthecrown.utils.PluginUtil;
import net.forthecrown.utils.Time;
import net.forthecrown.utils.math.Bounds3i;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.waypoints.WaypointScan.Result;
import net.forthecrown.waypoints.type.PlayerWaypointType;
import net.forthecrown.waypoints.type.WaypointType;
import net.forthecrown.waypoints.type.WaypointTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Snow;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.spongepowered.math.vector.Vector3i;

public final class Waypoints {
  private Waypoints() {}

  /* ------------------------- COLUMN CONSTANTS --------------------------- */

  public static final UUID NIL_UUID = new UUID(0, 0);

  public static final String POLE_STRUCTURE = "region_pole";
  public static final String FUNC_REGION_NAME = "region_name";
  public static final String FUNC_RESIDENTS = "region_residents";

  /**
   * Default size of the pole (5, 5, 5)
   */
  public static Vector3i DEFAULT_POLE_SIZE = Vector3i.from(5);

  public static BlockStructure getRegionPole() {
    return Structures.get().getRegistry().orNull(POLE_STRUCTURE);
  }

  public static Vector3i poleSize() {
    return Structures.get()
        .getRegistry()
        .get(POLE_STRUCTURE)
        .map(BlockStructure::getDefaultSize)
        .orElse(DEFAULT_POLE_SIZE);
  }

  public static void placePole(Waypoint region) {
    var structure = getRegionPole();

    if (structure == null) {
      Loggers.getLogger().warn("No pole structure found in registry! Cannot place!");
      return;
    }

    var config = StructurePlaceConfig.builder()
        .addNonNullProcessor()
        .addRotationProcessor()
        .world(region.getWorld())

        .pos(region.getBounds().min())

        // Function processors to ensure signs on pole
        // display correct information
        .addFunction(
            FUNC_REGION_NAME,
            (info, c) -> processTopSign(region, info, c)
        )
        .addFunction(
            FUNC_RESIDENTS,
            (info, c) -> processResidentsSign(region, info, c)
        )

        .build();

    structure.place(config);
  }

  private static void processTopSign(Waypoint region,
                                     FunctionInfo info,
                                     StructurePlaceConfig config
  ) {
    var pos = config.getTransform().apply(info.getOffset());
    var world = ((ImmediateBlockBuffer) config.getBuffer()).getWorld();

    var block = Vectors.getBlock(pos, world);

    org.bukkit.block.data.type.Sign signData =
        (org.bukkit.block.data.type.Sign)
            Material.OAK_SIGN.createBlockData();

    signData.setRotation(BlockFace.NORTH);
    block.setBlockData(signData, false);

    Sign sign = (Sign) block.getState();

    sign.line(1, signName(region));
    sign.line(2, text("Waypoint"));

    sign.update();
  }

  private static void processResidentsSign(Waypoint region,
                                           FunctionInfo info,
                                           StructurePlaceConfig config
  ) {
    if (region.get(WaypointProperties.HIDE_RESIDENTS) || region.getResidents().isEmpty()) {
      return;
    }

    var pos = config.getTransform().apply(info.getOffset());
    var world = ((ImmediateBlockBuffer) config.getBuffer()).getWorld();
    var block = Vectors.getBlock(pos, world);

    WallSign signData = (WallSign) Material.OAK_WALL_SIGN.createBlockData();
    signData.setFacing(info.getFacing().asBlockFace());
    block.setBlockData(signData);

    Sign sign = (Sign) block.getState();
    var residents = region.getResidents();

    if (residents.size() == 1) {
      sign.line(1, text("Resident:"));
      sign.line(2,
          Text.format("{0, user}",
              residents.keySet()
                  .iterator()
                  .next()
          )
      );
    } else {
      sign.line(1, text("Residents:"));
      sign.line(2, text(residents.size()));
    }

    sign.update();
  }

  private static Component signName(Waypoint waypoint) {
    var name = waypoint.get(WaypointProperties.NAME);
    return text(Strings.isNullOrEmpty(name) ? "Wilderness" : name);
  }

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
    return removeVulnerable(
        WaypointManager.getInstance()
            .getChunkMap()
            .get(world, pos)
    );
  }

  /**
   * Removes non-invulnerable waypoints from the given set
   */
  private static Set<Waypoint> removeVulnerable(Set<Waypoint> waypoints) {
    waypoints.removeIf(waypoint -> !waypoint.get(WaypointProperties.INVULNERABLE));
    return waypoints;
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
    for (var c: name.toCharArray()) {
      if (!StringReader.isAllowedInUnquotedString(c)) {
        return false;
      }
    }

    WaypointManager manager = WaypointManager.getInstance();
    Waypoint waypoint = manager.getExtensive(name);

    if (waypoint != null || BannedWords.contains(name)) {
      return false;
    }

    for (var e: manager.getExtensions()) {
      if (!e.isValidName(name)) {
        return false;
      }
    }

    return true;
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
   * @param pos         The position the waypoint will be placed at. Note that this parameter should
   *                    be shifted 1 block upward for region pole waypoints. As the platform
   *                    underneath the region pole is considered as the starting block, instead of
   *                    being under it. To ensure the above is the case use
   *                    {@link PlayerWaypointType#isValid(Waypoint)}, as that performs that
   *                    operation for you
   * @param type        The type to use for validation, this is used to test the column in the
   *                    center of the waypoint.
   * @param w           The world the waypoint is in.
   * @param testOverlap True, to ensure the given parameters do not overlap with another waypoint.
   * @return An empty optional if the area is valid, an optional containing a corresponding error
   * message, if the area is invalid
   */
  public static Optional<CommandSyntaxException> isValidWaypointArea(
      Vector3i pos,
      WaypointType type,
      World w,
      boolean testOverlap
  ) {
    Preconditions.checkArgument(type.isBuildable(), "Type is not buildable");

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

    if (testOverlap) {
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
   * Sets the waypoint's name sign.
   *
   * @param waypoint The waypoint to set the name sign of
   * @param name     The name to set the sign to, if null, the sign is removed
   * @throws IllegalStateException If the given waypoint is not a {@link PlayerWaypointType}
   */
  public static void setNameSign(Waypoint waypoint, String name)
      throws IllegalStateException
  {
    if (!(waypoint.getType() instanceof PlayerWaypointType type)) {
      throw new IllegalStateException(
          "Only player/guild waypoints can have manual name signs"
      );
    }

    Vector3i pos = waypoint.getPosition()
        .add(0, type.getColumn().length, 0);

    World w = waypoint.getWorld();
    Objects.requireNonNull(w, "World unloaded");

    Block b = Vectors.getBlock(pos, w);

    if (Strings.isNullOrEmpty(name)) {
      b.setType(Material.AIR);
    } else {
      b.setBlockData(Material.OAK_SIGN.createBlockData());

      Sign sign = (Sign) b.getState();
      sign.line(1, text(name));
      sign.line(2, text("Waypoint"));
      sign.update();
    }
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
   * @param source   The source attempting to create the waypoint.
   *
   * @return The created waypoint
   * @throws CommandSyntaxException If the waypoint creation fails at any stage
   */
  public static Waypoint tryCreate(CommandSource source) throws CommandSyntaxException {
    var player = source.asPlayer();
    User user = Users.get(player);
    Block b = WaypointTypes.findTopBlock(player);

    if (b == null) {
      throw WExceptions.FACE_WAYPOINT_TOP;
    }

    var config = WaypointManager.getInstance().config();
    if (config.isDisabledWorld(b.getWorld())) {
      throw WExceptions.WAYPOINTS_WRONG_WORLD;
    }

    PlayerWaypointType type = null;
    Vector3i pos = Vectors.from(b);

    Material topMaterial = b.getType();

    for (var potentialType: WaypointTypes.REGISTRY.values()) {
      if (!(potentialType instanceof PlayerWaypointType playerType)) {
        continue;
      }

      Material[] arr = playerType.getColumn();
      if (topMaterial != arr[arr.length - 1]) {
        continue;
      }

      type = playerType;
      break;
    }

    if (type == null) {
      throw WExceptions.invalidWaypointTop(topMaterial);
    }

    type.onCreate(user);

    var existing = WaypointManager.getInstance()
        .getChunkMap()
        .get(b.getWorld(), pos);

    Waypoint waypoint;
    if (existing.isEmpty()) {
      pos = pos.sub(0, type.getColumn().length - 1, 0);

      // Ensure the area is correct and validate the
      // center block column to ensure it's a proper waypoint
      var error = Waypoints.isValidWaypointArea(
          pos,
          type,
          b.getWorld(),
          true
      );

      if (error.isPresent()) {
        throw error.get();
      }

      waypoint = makeWaypoint(type, pos, source);
    } else {
      waypoint = existing.iterator().next();

      // Ensure the pole they're looking at is valid
      var error = waypoint.getType().isValid(waypoint);
      if (error.isPresent()) {
        throw error.get();
      }
    }

    type.onPostCreate(waypoint, user);
    return waypoint;
  }

  /**
   * Tests if the user can move their home waypoint.
   * <p>
   * If {@link WaypointConfig#moveInHasCooldown} is false, then this method will not throw an
   * exception.
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

    if (lastMoveIn == -1 || !config.moveInHasCooldown || config.moveInCooldown.toMillis() < 1) {
      return;
    }

    long remainingCooldown = Time.timeUntil(lastMoveIn + config.moveInCooldown.toMillis());

    if (remainingCooldown > 0) {
      throw Exceptions.cooldownEndsIn(remainingCooldown);
    }
  }

  public static Waypoint makeWaypoint(WaypointType type, Vector3i pos, CommandSource source) {
    Vector3i position;

    if (pos == null) {
      position = Vectors.intFrom(source.getLocation());
    } else {
      position = pos;
    }

    Waypoint waypoint = new Waypoint();
    waypoint.setType(type);
    waypoint.setPosition(position, source.getWorld());

    if (pos != null) {
      source.sendMessage(WMessages.createdWaypoint(position, type));
    } else {
      source.sendSuccess(WMessages.createdWaypoint(position, type));
    }

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
   * @see WaypointDynmap#updateMarker(Waypoint)
   */
  public static void updateDynmap(Waypoint waypoint) {
    if (!PluginUtil.isEnabled("Dynmap")) {
      return;
    }

    WaypointDynmap.updateMarker(waypoint);
  }

  public static Waypoint getHomeWaypoint(User user) {
    UUID homeId = user.get(WaypointPrefs.HOME_PROPERTY);
    if (NIL_UUID.equals(homeId)) {
      return null;
    }

    var manager = WaypointManager.getInstance();
    return manager.get(homeId);
  }
}