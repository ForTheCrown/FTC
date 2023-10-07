package net.forthecrown.waypoints.visit;

import static net.forthecrown.McConstants.MAX_Y;
import static net.forthecrown.waypoints.listeners.HulkSmash.GAME_TICKS_PER_COSMETIC_TICK;
import static net.kyori.adventure.util.Ticks.TICKS_PER_SECOND;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.entity.TeleportFlag.EntityState;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.command.Exceptions;
import net.forthecrown.cosmetics.CosmeticData;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.forthecrown.waypoints.Waypoint;
import net.forthecrown.waypoints.WaypointManager;
import net.forthecrown.waypoints.WaypointPrefs;
import net.forthecrown.waypoints.WaypointProperties;
import net.forthecrown.waypoints.Waypoints;
import net.forthecrown.waypoints.listeners.HulkSmash;
import net.forthecrown.waypoints.type.WaypointTypes;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spongepowered.math.vector.Vector3d;


@Getter
@Setter
public class WaypointVisit implements Runnable {

  /**
   * The user visiting the region
   */
  private final User user;

  /**
   * The user's active travel effect
   */
  private final TravelEffect activeEffect;

  /**
   * Visitation handlers
   */
  private final Set<VisitHandler> handlers = new ObjectOpenHashSet<>();

  /**
   * Predicates to pass in order to visit
   */
  private final Set<VisitPredicate> predicates = new ObjectOpenHashSet<>();

  /**
   * Region manager
   */
  private final WaypointManager manager;

  /**
   * The destination region
   */
  private final Waypoint destination;

  /**
   * The local region, null, if user is in a non-region world
   */
  private final Waypoint nearestWaypoint;

  /**
   * The location the user will be teleported to, will either be in the sky above the destination's
   * pole or one block above it depending on the result of {@link #hulkSmash()}
   */
  private Location teleportLocation;

  /**
   * True, if the user is near the local region's pole, false otherwise
   */
  private final boolean nearWaypoint;

  /**
   * Set by a {@link VisitHandler}, determines if one of the handlers will manually teleport the
   * player themselves
   */
  @Getter(AccessLevel.PACKAGE) @Setter(AccessLevel.PACKAGE)
  private boolean cancelTeleport = false;

  /**
   * Hulk smash state of this visit
   */
  @Getter
  @Accessors(fluent = true)
  private boolean hulkSmash;

  public WaypointVisit(
      User user,
      Waypoint destination,
      WaypointManager manager
  ) {
    this.user = user;
    this.activeEffect = user.getComponent(CosmeticData.class)
        .getValue(Cosmetics.TRAVEL_EFFECTS);

    this.destination = destination;
    this.manager = manager;

    this.nearestWaypoint = manager.getChunkMap()
        .findNearest(user.getLocation())
        .first();

    findInitialTeleport();

    // If the user is near the pole determined by seeing if the user is in
    // the same world as the region pole and less than DISTANCE_TO_POLE
    // from the pole. Mainly used by OwnedEntityHandler to check if it should
    // scan for entities or not. Pole distance check for non staff players
    // is run in CommandVisit
    nearWaypoint = nearestWaypoint != null
        && nearestWaypoint.getBounds().contains(user.getPlayer());

    // For hulk smashing to be allowed we must pass 5 checks:
    // 1) server allows hulk smashing
    // 2) user allows hulk smashing
    // 3) The area above the pole must only be air
    // 4) The area above the user must only be air
    // 5) The user shouldn't be in spectator mode
    hulkSmash = WaypointManager.getInstance().config().hulkSmashPoles
        && user.get(WaypointPrefs.HULK_SMASH_ENABLED)
        && isClearAbove(getTeleportLocation())
        && isClearAbove(user.getLocation().add(0, Math.ceil(user.getPlayer().getHeight()), 0))
        && user.getGameMode() != GameMode.SPECTATOR;
  }

  static boolean isClearAbove(Location l) {
    int x = l.getBlockX();
    int z = l.getBlockZ();

    Block block;

    for (int y = l.getBlockY(); y < MAX_Y; y++) {
      block = l.getWorld().getBlockAt(x, y, z);

      if (block.isCollidable()) {
        return false;
      }
    }

    return true;
  }

  /**
   * Makes the given user visit the given region
   * <p>
   * Instantiates a {@link WaypointVisit} instance, adds the 2 predicates declared in static methods
   * in {@link VisitPredicate} and adds the {@link VehicleVisitHandler} and
   * {@link OwnedEntityHandler} to the visit then calls {@link #run()}
   *
   * @param visitor The user visiting the region
   * @param region  The region being visited
   */
  public static void visit(User visitor, Waypoint region) {
    new WaypointVisit(visitor, region, WaypointManager.getInstance())
        .addPredicate(VisitPredicate.RIDING_VEHICLE)
        .addPredicate(VisitPredicate.IS_NEAR)
        .addPredicate(VisitPredicate.DESTINATION_VALID)
        .addPredicate(VisitPredicate.NEAREST_VALID)

        // This order matters, vehicles must be handled before
        // other passengers
        .addHandler(new VehicleVisitHandler())
        .addHandler(new OwnedEntityHandler())

        .run();
  }

  public WaypointVisit addHandler(VisitHandler handler) {
    handlers.add(handler);
    return this;
  }

  public WaypointVisit addPredicate(VisitPredicate predicate) {
    predicates.add(predicate);
    return this;
  }

  public Location getTeleportLocation() {
    return teleportLocation == null ? null : teleportLocation.clone();
  }

  void findInitialTeleport() {
    Vector3d tpDest = getDestination()
        .getType()
        .getVisitPosition(getDestination());

    Location playerLoc = user.getLocation();

    setTeleportLocation(
        new Location(
            destination.getWorld(),
            tpDest.x(), tpDest.y(), tpDest.z(),
            playerLoc.getYaw(), playerLoc.getPitch()
        )
    );
  }

  /**
   * Changes the state of this visit's hulk smash, if and only if, hulk-smashing is not already
   * disabled, if it is, this method does nothing
   *
   * @param hulkSmash True, to allow hulk smashing, false otherwise
   */
  public void setHulkSmashSafe(boolean hulkSmash) {
    hulkSmash(hulkSmash && hulkSmash());
  }

  @Override
  public void run() {
    try {
      for (VisitPredicate p : getPredicates()) {
        p.test(this);
      }
    } catch (CommandSyntaxException e) {
      Exceptions.handleSyntaxException(getUser(), e);
      return;
    }

    // Make sure destination has region pole, if dest is region
    // pole and invulnerable
    if (destination.getType() == WaypointTypes.REGION_POLE
        && destination.get(WaypointProperties.INVULNERABLE)
    ) {
      Waypoints.placePole(destination);
    }

    // Run start handlers
    runHandlers(h -> h.onStart(this));

    Player player = user.getPlayer();
    Entity teleportedEntity = findRootEntity(player);

    if (!Objects.equals(teleportedEntity, player)) {
      hulkSmash = false;
    }

    if (hulkSmash()) {
      // Change teleport location
      Location tpLoc = getTeleportLocation();
      tpLoc.setY(MAX_Y);
      tpLoc.setPitch(90f);

      // getTeleportLocation returns a clone, so we've got to set it again
      setTeleportLocation(tpLoc);

      // Shoot you up into the sky lol
      user.getPlayer().setVelocity(new Vector(0, 20, 0));
      user.playSound(Sound.ENTITY_ENDER_DRAGON_SHOOT, 1, 1.2f);

      // If they have cosmetic effect, execute it
      if (activeEffect != null) {
        activeEffect.onHulkStart(user, user.getLocation());
      }

      // Run the goingUp thing, so we can run the cosmetic effects while
      // they're ascending and then teleport them after they've reached
      // their peek
      Tasks.runTimer(
          new GoingUp(),
          GAME_TICKS_PER_COSMETIC_TICK,
          GAME_TICKS_PER_COSMETIC_TICK
      );
    } else {
      // Execute travel effect, if they have one
      if (shouldPlayEffect()) {
        Tasks.runLater(() -> {
          activeEffect.onPoleTeleport(
              user,
              user.getLocation(),
              getTeleportLocation()
          );
        }, 2);
      }

      if (!cancelTeleport) {
        // Just TP them to pole... boring
        teleportedEntity.teleport(
            getTeleportLocation(),
            EntityState.RETAIN_PASSENGERS
        );

        user.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
      }

      runTpHandlers();
    }
  }

  boolean shouldPlayEffect() {
    return activeEffect != null && user.getGameMode() != GameMode.SPECTATOR;
  }

  void runHandlers(Consumer<VisitHandler> consumer) {
    if (getHandlers().isEmpty()) {
      return;
    }

    getHandlers().forEach(consumer);
  }

  void runTpHandlers() {
    runHandlers(h -> h.onTeleport(this));
  }

  private Entity findRootEntity(Entity entity) {
    var root = entity;

    while (root.getVehicle() != null) {
      root = root.getVehicle();
    }

    return root;
  }

  private class GoingUp implements Consumer<BukkitTask> {

    int tick = (int) (0.75 * (TICKS_PER_SECOND / GAME_TICKS_PER_COSMETIC_TICK));

    @Override
    public void accept(BukkitTask task) {
      try {
        // If they have travel effect, run it
        if (activeEffect != null) {
          activeEffect.onHulkTickUp(user, user.getLocation());
        }

        // If we're below the tick limit,
        // stop and move on to fall listener
        if (--tick < 0) {
          task.cancel();

          user.getPlayer().teleport(
              getTeleportLocation(),
              EntityState.RETAIN_PASSENGERS
          );

          user.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
          runTpHandlers();
          HulkSmash.startHulkSmash(user, activeEffect);
        }
      } catch (Exception e) {
        task.cancel();
      }
    }
  }
}