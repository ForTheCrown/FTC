package net.forthecrown.waypoint.visit.handlers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.forthecrown.user.User;
import net.forthecrown.utils.Tasks;
import net.forthecrown.utils.math.WorldBounds3i;
import net.forthecrown.waypoint.visit.VisitHandler;
import net.forthecrown.waypoint.visit.WaypointVisit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OwnedEntityHandler implements VisitHandler {
    public ObjectSet<UUID> ignored = new ObjectOpenHashSet<>();
    final List<Entity> toTeleport = new ObjectArrayList<>();
    private Location entityTpLocation;

    @Override
    public void onStart(WaypointVisit visit) {
        // if the visitor isn't even near the pole
        // there's no point in setting an entity tp location
        // or scanning for entities
        if (!visit.isNearWaypoint()) {
            return;
        }

        // initial teleport location will be right above pole
        // if hulk smash is true, teleport loc will be reassigned later
        // that's a no no
        entityTpLocation = visit.getTeleportLocation();

        WorldBounds3i box = visit.getNearestWaypoint()
                .getBounds()
                .toWorldBounds(visit.getNearestWaypoint().getWorld())
                .expand(1);

        User user = visit.getUser();

        toTeleport.addAll(
                box.getEntities(e -> {
                    if (ignored.contains(e.getUniqueId())
                            || e.isInsideVehicle()
                            || e.getType() == EntityType.PLAYER
                    ) {
                        return false;
                    }

                    //If the entity is tameable and has been tamed
                    //by the visitor
                    if (e instanceof Tameable tameable) {
                        if (!tameable.isTamed()
                                || tameable.getOwnerUniqueId() == null
                        ) {
                            return false;
                        }

                        return Objects.equals(
                                tameable.getOwnerUniqueId(),
                                user.getUniqueId()
                        );
                    }

                    //If they're leashed by the visitor
                    if (e instanceof LivingEntity living) {
                        if (!living.isLeashed()) {
                            return false;
                        }

                        Entity leashHolder = living.getLeashHolder();

                        return leashHolder.getUniqueId()
                                .equals(user.getUniqueId());
                    }

                    return false;
                })
        );

        visit.setHulkSmashSafe(toTeleport.isEmpty());
    }

    @Override
    public void onTeleport(WaypointVisit visit) {
        // Teleport all found entities
        tpDelayed(toTeleport, entityTpLocation);
    }

    // If we tp'd all entities just after the user teleported
    // the entities would go into limbo because the chunks might
    // not be loaded, so we have to delay the teleport half a
    // second so the chunks can be loaded and the entities won't
    // go missing.
    // Kinda similar to what happened to the Wither farm me and
    // Robin tried to make.
    static void tpDelayed(List<Entity> entities, Location location) {
        if (location == null || entities.isEmpty()) {
            return;
        }

        Tasks.runLater(
                () -> entities.forEach(e -> e.teleport(location)),
                10
        );
    }
}