package net.forthecrown.regions.visit.handlers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.forthecrown.core.Crown;
import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.regions.visit.VisitHandler;
import net.forthecrown.user.User;
import net.forthecrown.utils.math.WorldBounds3i;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Tameable;

import java.util.List;
import java.util.UUID;

public class OwnedEntityHandler implements VisitHandler {
    public ObjectSet<UUID> ignored = new ObjectOpenHashSet<>();
    final List<Entity> toTeleport = new ObjectArrayList<>();
    boolean hasLeashed = false;
    private Location entityTpLocation;

    @Override
    public void onStart(RegionVisit visit) {
        // if the visitor isn't even near the pole
        // there's no point in setting an entity tp location
        // or scanning for entities
        if (!visit.isNearPole()) {
            return;
        }

        // initial teleport location will be right above pole
        // if hulk smash is true, teleport loc will be reassigned later
        // that's a no no
        entityTpLocation = visit.getTeleportLocation();

        WorldBounds3i box = visit.getLocalRegion().getPoleBounds().toWorldBounds(visit.getDestWorld());
        box.expand(1);

        User user = visit.getUser();

        toTeleport.addAll(
                box.getEntities(e -> {
                    if (ignored.contains(e.getUniqueId())) {
                        return false;
                    }

                    // Don't move entities inside vehicles
                    if (e.isInsideVehicle()) {
                        return false;
                    }

                    //Skip players
                    if (e.getType() == EntityType.PLAYER) {
                        return false;
                    }

                    //If the entity is tameable and has been tamed
                    //by the visitor
                    if(e instanceof Tameable) {
                        Tameable tameable = (Tameable) e;
                        if(!tameable.isTamed()) return false;

                        if(tameable.getOwnerUniqueId().equals(user.getUniqueId())) {
                            return true;
                        }
                    }

                    //If they're leashed by the visitor
                    if(e instanceof LivingEntity) {
                        LivingEntity living = (LivingEntity) e;

                        try {
                            Entity leashHolder = living.getLeashHolder();
                            if(leashHolder.getUniqueId().equals(user.getUniqueId())) {
                                hasLeashed = true;
                                return true;
                            }

                        } catch (IllegalStateException e1) {
                        }
                    }

                    return false;
                })
        );

        // If you've got a leashed entity, teleporting might be a bit
        // dodgy, so set it to false
        visit.setHulkSmashSafe(!hasLeashed);
    }

    @Override
    public void onTeleport(RegionVisit visit) {
        // Teleport all found entities
        tpDelayed(toTeleport, entityTpLocation);
    }

    // Enjoy this essay of a comment lmao
    //
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

        Bukkit.getScheduler().runTaskLater(Crown.plugin(), () -> {
            entities.forEach(e -> e.teleport(location));
        }, 10);
    }
}