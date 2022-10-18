package net.forthecrown.regions.visit;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.forthecrown.commands.manager.Exceptions;
import net.forthecrown.core.Vars;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.events.dynamic.HulkSmashListener;
import net.forthecrown.regions.PopulationRegion;
import net.forthecrown.regions.RegionManager;
import net.forthecrown.regions.Regions;
import net.forthecrown.regions.visit.handlers.OwnedEntityHandler;
import net.forthecrown.regions.visit.handlers.RidingVehicleHandler;
import net.forthecrown.user.User;
import net.forthecrown.user.property.Properties;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.Util;
import net.kyori.adventure.util.Ticks;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.spongepowered.math.vector.Vector3i;

import java.util.Set;
import java.util.function.Consumer;


@Getter @Setter
public class RegionVisit implements Runnable {
    /** The user visiting the region */
    private final User user;

    /** The user's active travel effect */
    private final TravelEffect activeEffect;

    /** Visitation handlers */
    private final Set<VisitHandler> handlers = new ObjectOpenHashSet<>();

    /** Predicates to pass in order to visit */
    private final Set<VisitPredicate> predicates = new ObjectOpenHashSet<>();

    /** Region manager */
    private final RegionManager manager;

    /** The destination region */
    private final PopulationRegion region;

    /** The local region, null, if user is in a non-region world */
    private final PopulationRegion localRegion;

    /**
     * The location the user will be teleported to, will either be
     * in the sky above the destination's pole or one block above
     * it depending on the result of {@link #hulkSmash()}
     */
    private Location teleportLocation;

    /** True, if the user is near the local region's pole, false otherwise */
    private final boolean nearPole;

    /** Hulk smash state of this visit */
    @Getter
    @Accessors(fluent = true)
    private boolean hulkSmash;

    public RegionVisit(User user, PopulationRegion region, RegionManager manager) {
        this.user = user;
        this.activeEffect = user.getCosmeticData().get(Cosmetics.TRAVEL);

        this.region = region;
        this.manager = manager;
        this.localRegion = manager.get(user.getRegionPos());

        findInitialTeleport();

        // If the user is near the pole determined by seeing if the user is in
        // the same world as the region pole and less than DISTANCE_TO_POLE
        // from the pole. Mainly used by OwnedEntityHandler to check if it should
        // scan for entities or not. Pole distance check for non staff players
        // is ran in CommandVisit
        nearPole = Regions.isCloseToPole(localRegion.getPolePosition(), user)
                && getOriginWorld().equals(getDestWorld());

        // For hulk smashing to be allowed we must pass 4 checks:
        // 1) server allows hulk smashing
        // 2) user allows hulk smashing
        // 3) The area above the pole must only be air
        // 4) The area above the user must only be air
        // 5) The user shouldn't be in spectator mode
        hulkSmash = Vars.hulkSmashPoles
                && user.get(Properties.HULK_SMASHING)
                && Util.isClearAbove(getTeleportLocation())
                && Util.isClearAbove(user.getLocation().add(0, Math.ceil(user.getPlayer().getHeight()), 0))
                && user.getGameMode() != GameMode.SPECTATOR;
    }

    /**
     * Makes the given user visit the given region
     * <p>
     * Instantiates a {@link RegionVisit} instance, adds
     * the 2 predicates declared in static methods in {@link VisitPredicate}
     * and adds the {@link RidingVehicleHandler} and {@link OwnedEntityHandler}
     * to the visit then calls {@link #run()}
     *
     * @param visitor The user visiting the region
     * @param region The region being visited
     */
    public static void visitRegion(User visitor, PopulationRegion region) {
        new RegionVisit(visitor, region, RegionManager.get())
                .addPredicate(VisitPredicate.ensureRidingVehicle())
                .addPredicate(VisitPredicate.ensureNoPassengers())

                // This order matters, vehicles must be handled before
                // other passengers
                .addHandler(new RidingVehicleHandler())
                //.addHandler(new PassengerHandler())
                .addHandler(new OwnedEntityHandler())

                .run();
    }

    public RegionVisit addHandler(VisitHandler handler) {
        handlers.add(handler);
        return this;
    }

    public RegionVisit addPredicate(VisitPredicate predicate) {
        predicates.add(predicate);
        return this;
    }

    public Location getTeleportLocation() {
        return teleportLocation == null ? null : teleportLocation.clone();
    }

    void findInitialTeleport() {
        Vector3i poleBottom = getRegion().getPoleBottom().add(0, 5, 0);
        Location playerLoc = user.getLocation();

        setTeleportLocation(
                new Location(
                        playerLoc.getWorld(),
                        poleBottom.x(), poleBottom.y(), poleBottom.z(),
                        playerLoc.getYaw(), playerLoc.getPitch()
                )
        );
    }

    public World getDestWorld() {
        return getManager().getWorld();
    }

    public World getOriginWorld() {
        return getUser().getWorld();
    }

    public void setHulkSmashSafe(boolean hulkSmash) {
        hulkSmash(hulkSmash && hulkSmash());
    }

    public <T extends VisitHandler> T getHandler(Class<T> clazz) {
        for (VisitHandler h: getHandlers()) {
            if(h.getClass().equals(clazz)) return (T) h;
        }

        return null;
    }

    @Override
    public void run() {
        try {
            for (VisitPredicate p: getPredicates()) {
                p.test(this);
            }
        } catch (CommandSyntaxException e) {
            Exceptions.handleSyntaxException(getUser(), e);
            return;
        }

        // Make sure destination has region pole
        Regions.placePole(region);

        // Run start handlers
        runHandlers(h -> h.onStart(this));

        if (hulkSmash()) {
            // Change teleport location
            Location tpLoc = getTeleportLocation();
            tpLoc.setY(Util.MAX_Y);
            tpLoc.setPitch(90f);

            // getTeleportLocation returns a clone, so we've got to set it again
            setTeleportLocation(tpLoc);

            // Shoot you up into the sky lol
            user.getPlayer().setVelocity(new Vector(0, 20, 0));

            // If they have cosmetic effect, execute it
            if (activeEffect != null) {
                activeEffect.onHulkStart(user, user.getLocation());
            }

            // Run the goingUp thing, so we can run the cosmetic effects while
            // they're ascending and then teleport them after they've reached
            // their peek
            Tasks.runTimer(
                    new GoingUp(),
                    HulkSmashListener.TICKS_PER_TICK, HulkSmashListener.TICKS_PER_TICK
            );
        } else {
            // Execute travel effect, if they have one
            if(activeEffect != null) {
                Tasks.runLater(() -> {
                    activeEffect.onPoleTeleport(user, user.getLocation(), getTeleportLocation());
                }, 2);
            }

            // Just TP them to pole... boring
            user.getPlayer().teleport(getTeleportLocation());

            runTpHandlers();
        }
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

    private class GoingUp implements Consumer<BukkitTask> {
        byte tick = (byte) (0.75 * (Ticks.TICKS_PER_SECOND / HulkSmashListener.TICKS_PER_TICK));

        @Override
        public void accept(BukkitTask task) {
            try {
                // If they have travel effect, run it
                if (activeEffect != null) {
                    activeEffect.onHulkTickUp(user, user.getLocation());
                }

                // If we're below the tick limit, stop and move on to fall listener
                if (--tick < 0) {
                    task.cancel();

                    user.getPlayer().teleport(getTeleportLocation());

                    runTpHandlers();

                    HulkSmashListener listener = new HulkSmashListener(user, getActiveEffect());
                    listener.beginListening();
                }
            } catch (Exception e) {
                task.cancel();
            }
        }
    }
}