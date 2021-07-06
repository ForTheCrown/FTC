package net.forthecrown.cosmetics;

import net.forthecrown.core.CrownCore;
import net.forthecrown.cosmetics.arrows.ArrowEffects;
import net.forthecrown.cosmetics.deaths.DeathEffects;
import net.forthecrown.cosmetics.emotes.Emotes;

public final class Cosmetics {

    private static PlayerRidingManager rideManager;

    private Cosmetics() {}

    public static void init(){
        rideManager = new PlayerRidingManager();

        Emotes.init();
        ArrowEffects.init();
        DeathEffects.init();

        CrownCore.logger().info("Cosmetics loaded");
    }

    public static void shutDown(){
        rideManager.stopAllRiding();
    }

    public static PlayerRidingManager getRideManager() {
        return rideManager;
    }
}