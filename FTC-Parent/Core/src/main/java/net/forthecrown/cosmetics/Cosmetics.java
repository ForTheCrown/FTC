package net.forthecrown.cosmetics;

public final class Cosmetics {

    private static PlayerRidingManager rideManager;

    private Cosmetics() {}

    public static void init(){
        rideManager = new PlayerRidingManager();
    }

    public static void shutDown(){
        rideManager.stopAllRiding();
    }

    public static PlayerRidingManager getRideManager() {
        return rideManager;
    }
}