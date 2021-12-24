package net.forthecrown.regions.visit;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.forthecrown.core.ComVars;
import net.forthecrown.core.Crown;
import net.forthecrown.cosmetics.travel.TravelEffect;
import net.forthecrown.events.dynamic.RegionVisitListener;
import net.forthecrown.regions.*;
import net.forthecrown.user.CrownUser;
import net.forthecrown.user.data.FtcGameMode;
import net.forthecrown.utils.FtcUtils;
import net.forthecrown.utils.math.WorldVec3i;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.function.Consumer;

public class RegionVisit implements Runnable {
    private final CrownUser user;
    private final TravelEffect activeEffect;

    private final Set<VisitHandler> handlers = new ObjectOpenHashSet<>();
    private final Set<VisitPredicate> predicates = new ObjectOpenHashSet<>();

    private final RegionManager manager;

    private final PopulationRegion region;
    private final PopulationRegion localRegion;

    private Location teleportLocation;
    private final boolean nearPole;
    private boolean hulkSmash;

    public RegionVisit(CrownUser user, PopulationRegion region, RegionManager manager) {
        this.user = user;
        this.activeEffect = user.getCosmeticData().getActiveTravel();

        this.region = region;
        this.manager = manager;
        this.localRegion = manager.get(user.getRegionCords());

        findInitialTeleport();

        // If the user is near the pole determined by seeing if the user is in
        // the same world as the region pole and less than DISTANCE_TO_POLE
        // from the pole. Mainly used by OwnedEntityHandler to check if it should
        // scan for entities or not
        nearPole = RegionUtil.isCloseToPole(localRegion.getPolePosition(), user) && getOriginWorld().equals(getDestWorld());

        // For hulk smashing to be allowed we must pass 4 checks:
        // The first two are obvious lol
        // 3) The pole must only be air
        // 4) The area above the user must only be air
        hulkSmash = ComVars.shouldHulkSmashPoles()
                && user.hulkSmashesPoles()
                && FtcUtils.isClearAbove(getTeleportLocation())
                && FtcUtils.isClearAbove(user.getLocation())
                && user.getGameMode() != FtcGameMode.SPECTATOR;
    }

    public CrownUser getUser() {
        return user;
    }

    public PopulationRegion getRegion() {
        return region;
    }

    public Set<VisitHandler> getHandlers() {
        return handlers;
    }

    public RegionVisit addHandler(VisitHandler handler) {
        handlers.add(handler);
        return this;
    }

    public Set<VisitPredicate> getPredicates() {
        return predicates;
    }

    public RegionVisit addPredicate(VisitPredicate predicate) {
        predicates.add(predicate);
        return this;
    }

    public Location getTeleportLocation() {
        return teleportLocation == null ? null : teleportLocation.clone();
    }

    public void setTeleportLocation(Location teleportLocation) {
        this.teleportLocation = teleportLocation;
    }

    void findInitialTeleport() {
        WorldVec3i poleBottom = RegionUtil.bottomOfPole(getDestWorld(), getRegion().getPolePosition());
        Location playerLoc = user.getLocation();

        setTeleportLocation(
                poleBottom.add(0, 5, 0).toLocation(
                        playerLoc.getYaw(), playerLoc.getPitch()
                ).toCenterLocation()
        );
    }

    public boolean isNearPole() {
        return nearPole;
    }

    public PopulationRegion getLocalRegion() {
        return localRegion;
    }

    public RegionManager getManager() {
        return manager;
    }

    public RegionPoleGenerator getGenerator() {
        return getManager().getGenerator();
    }

    public World getDestWorld() {
        return getManager().getWorld();
    }

    public World getOriginWorld() {
        return getUser().getWorld();
    }

    public void setHulkSmash(boolean hulkSmash) {
        this.hulkSmash = hulkSmash;
    }

    public void setHulkSmashSafe(boolean hulkSmash) {
        setHulkSmash(hulkSmash && hulkSmash());
    }

    public boolean hulkSmash() {
        return hulkSmash;
    }

    public TravelEffect getActiveEffect() {
        return activeEffect;
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
            FtcUtils.handleSyntaxException(getUser(), e);
            return;
        }

        // Make sure destination has region pole
        getGenerator().generate(getRegion());

        // Run start handlers
        runHandlers(h -> h.onStart(this));

        if(hulkSmash()) {
            // Change teleport location
            Location tpLoc = getTeleportLocation();
            tpLoc.setY(FtcUtils.MAX_Y);
            tpLoc.setPitch(90f);

            // getTeleportLocation returns a clone, so we've got to set it again
            setTeleportLocation(tpLoc);

            // Shoot you up into the sky lol
            user.setVelocity(0, 20, 0);

            // If they have cosmetic effect, execute it
            if(activeEffect != null) activeEffect.onHulkStart(user, user.getLocation());

            // Run the goingUp thing, so we can run the cosmetic effects while
            // they're ascending and then teleport them after they've reached
            // their peek
            Bukkit.getScheduler().runTaskTimer(
                    Crown.inst(), new GoingUp(),
                    RegionVisitListener.TICKS_PER_TICK, RegionVisitListener.TICKS_PER_TICK
            );
        } else {
            // Execute travel effect, if they have one
            if(activeEffect != null) {
                Bukkit.getScheduler().runTaskLater(Crown.inst(), () -> {
                    activeEffect.onPoleTeleport(user, user.getLocation(), getTeleportLocation());
                }, 2);
            }

            // Just TP them to pole... boring
            user.getPlayer().teleport(getTeleportLocation());

            runHandlers(h -> h.onTeleport(this));
        }
    }

    void runHandlers(Consumer<VisitHandler> consumer) {
        if(getHandlers().isEmpty()) return;
        getHandlers().forEach(consumer);
    }

    private class GoingUp implements Consumer<BukkitTask> {
        byte tick = (byte) (0.75 * (20 / RegionVisitListener.TICKS_PER_TICK));

        @Override
        public void accept(BukkitTask task) {
            try {
                // If they have travel effect, run it
                if(activeEffect != null) activeEffect.onHulkTickUp(user, user.getLocation());

                tick--;

                // If we're below the tick limit, stop and move on to fall listener
                if(tick < 0) {
                    task.cancel();

                    user.getPlayer().teleport(getTeleportLocation());

                    runHandlers(h -> h.onTeleport(RegionVisit.this));

                    RegionVisitListener listener = new RegionVisitListener(user, getActiveEffect());
                    listener.beginListening();
                }
            } catch (Exception e) {
                task.cancel();
            }
        }
    }
}
