package net.forthecrown.regions.visit.handlers;

import net.forthecrown.regions.visit.RegionVisit;
import net.forthecrown.regions.visit.VisitHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

public class RidingVehicleHandler implements VisitHandler {
    PassengerHandler.PassengerInfo info = new PassengerHandler.PassengerInfo();

    @Override
    public void onStart(RegionVisit visit) {
        Player player = visit.getUser().getPlayer();
        boolean riding = ridingVehicle(player);
        visit.setHulkSmashSafe(!riding);

        if (riding) {
            Entity vehicle = player.getVehicle();
            info.fillFrom(vehicle, visit);
        }
    }

    @Override
    public void onTeleport(RegionVisit visit) {
        if (info.isEmpty()) {
            return;
        }

        info.movePassengers();
    }

    boolean ridingVehicle(Player player) {
        if (!player.isInsideVehicle()) {
            return false;
        }

        return player.getVehicle() instanceof Vehicle;
    }
}