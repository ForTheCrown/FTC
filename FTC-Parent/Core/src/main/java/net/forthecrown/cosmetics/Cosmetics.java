package net.forthecrown.cosmetics;

import net.forthecrown.core.ForTheCrown;
import net.forthecrown.cosmetics.arrows.ArrowEffects;
import net.forthecrown.cosmetics.deaths.DeathEffects;
import net.forthecrown.cosmetics.emotes.CosmeticEmotes;

public final class Cosmetics {

    private static PlayerRidingManager rideManager;

    private Cosmetics() {}

    public static void init(){
        rideManager = new PlayerRidingManager();

        CosmeticEmotes.init();
        ArrowEffects.init();
        DeathEffects.init();

        ForTheCrown.logger().info("Cosmetics loaded");
    }

    public static void shutDown(){
        rideManager.stopAllRiding();
    }

    public static PlayerRidingManager getRideManager() {
        return rideManager;
    }
}