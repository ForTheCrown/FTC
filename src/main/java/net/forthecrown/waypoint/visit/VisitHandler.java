package net.forthecrown.waypoint.visit;

public interface VisitHandler {
    /**
     * Will be called right after the region visit is ran,
     * use it to set up any events or code for {@link VisitHandler#onTeleport(WaypointVisit)}
     * @param visit The visit
     */
    void onStart(WaypointVisit visit);

    /**
     * If hulk smash = true, will be called after the user it teleported over
     * the destination pole,
     * if false, will be called right after the player is teleported
     * @param visit The visit
     */
    void onTeleport(WaypointVisit visit);
}