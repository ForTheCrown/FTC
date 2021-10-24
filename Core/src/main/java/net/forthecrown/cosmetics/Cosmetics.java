package net.forthecrown.cosmetics;

import net.forthecrown.core.Crown;
import net.forthecrown.cosmetics.arrows.ArrowEffects;
import net.forthecrown.cosmetics.deaths.DeathEffects;
import net.forthecrown.cosmetics.emotes.CosmeticEmotes;
import net.forthecrown.cosmetics.travel.TravelEffects;

public final class Cosmetics {
    private Cosmetics() {}

    private static PlayerRidingManager rideManager;

    public static void init() {
        rideManager = new PlayerRidingManager();

        CosmeticEmotes.init();
        ArrowEffects.init();
        DeathEffects.init();
        TravelEffects.init();

        Crown.logger().info("Cosmetics loaded");
    }

    public static void shutDown() {
        rideManager.stopAllRiding();
    }

    public static PlayerRidingManager getRideManager() {
        return rideManager;
    }
}