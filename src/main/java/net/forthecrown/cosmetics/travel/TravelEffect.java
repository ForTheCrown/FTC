package net.forthecrown.cosmetics.travel;

import net.forthecrown.cosmetics.Cosmetic;
import net.forthecrown.cosmetics.Cosmetics;
import net.forthecrown.utils.inventory.menu.Slot;
import net.forthecrown.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;

public abstract class TravelEffect extends Cosmetic {
    TravelEffect(String name, Slot cords, Component... description) {
        super(name, Cosmetics.TRAVEL, cords, description);
    }

    public abstract void onPoleTeleport(User user, Location from, Location pole);

    public abstract void onHulkStart(User user, Location loc);
    public abstract void onHulkTickDown(User user, Location loc);
    public abstract void onHulkTickUp(User user, Location loc);
    public abstract void onHulkLand(User user, Location landing);
}