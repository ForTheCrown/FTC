package net.forthecrown.regions.visit;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.Exceptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;

public interface VisitPredicate {
    /**
     * Tests if the visit is allowed to continue
     * <p></p>
     * Predicates are the first thing called when a
     * region visit is ran
     * @param visit The visit to check
     * @throws CommandSyntaxException If the check failed
     */
    void test(RegionVisit visit) throws CommandSyntaxException;

    static VisitPredicate ensureRidingVehicle() {
        return visit -> {
            Entity entity = visit.getUser().getPlayer().getVehicle();
            if (entity == null || entity instanceof Vehicle) {
                return;
            }

            throw Exceptions.ONLY_IN_VEHICLE;
        };
    }

    static VisitPredicate ensureNoPassengers() {
        return visit -> {
            if(!visit.getUser().getPlayer().getPassengers().isEmpty()) {
                throw Exceptions.CANNOT_BE_RIDDEN;
            }
        };
    }
}