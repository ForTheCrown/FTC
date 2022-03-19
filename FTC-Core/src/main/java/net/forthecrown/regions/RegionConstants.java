package net.forthecrown.regions;

/**
 * Class for storing constants related to regions
 */
public final class RegionConstants {
    private RegionConstants() {}

    /**
     * The full width of a region
     */
    public static final int REGION_SIZE = 400;

    /**
     * Half of a region's width
     */
    public static final int HALF_REGION_SIZE = REGION_SIZE / 2;

    /**
     * The max distance a player can be be to use a pole
     */
    public static final float DISTANCE_TO_POLE = 3f;

    /**
     * The default name of the server's spawn region
     */
    public static final String DEFAULT_SPAWN_NAME = "Hazelguard";
}
