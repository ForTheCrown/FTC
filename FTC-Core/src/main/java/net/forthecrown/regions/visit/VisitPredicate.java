package net.forthecrown.regions.visit;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.forthecrown.commands.manager.FtcExceptionProvider;
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
            if(entity == null || entity instanceof Vehicle) return;

            throw FtcExceptionProvider.create("Can only teleport while on a rideable entity");
        };
    }

    static VisitPredicate ensureNoPassengers() {
        return visit -> {
            if(!visit.getUser().getPlayer().getPassengers().isEmpty()) {
                throw FtcExceptionProvider.create("Cannot have players riding you to teleport");
            }
        };
    }
}
