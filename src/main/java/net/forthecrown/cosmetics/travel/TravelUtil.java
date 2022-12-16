package net.forthecrown.cosmetics.travel;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public final class TravelUtil {
    private TravelUtil() {}

    private static final float[][] HEART_OFFSETS = {
            // Bottom Tips:
            { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 },

            // Right straights:
            {  0.25f,  1.25f }, {  0.5f,  1.5f }, {  0.75f,  1.75f }, {  1,  2 },
            {  0.25f, -1.25f }, {  0.5f, -1.5f }, {  0.75f, -1.75f }, {  1, -2 },
            {  1.25f,  0.25f }, {  1.5f,  0.5f }, {  1.75f,  0.75f }, {  2,  1 },
            { -1.25f,  0.25f }, { -1.5f,  0.5f }, { -1.75f,  0.75f }, { -2,  1 },

            // Left straights:
            { -0.25f,  1.25f }, { -0.5f,  1.5f }, { -0.75f,  1.75f }, { -1,  2 },
            { -0.25f, -1.25f }, { -0.5f, -1.5f }, { -0.75f, -1.75f }, { -1, -2 },
            {  1.25f, -0.25f }, {  1.5f, -0.5f }, {  1.75f, -0.75f }, {  2, -1 },
            { -1.25f, -0.25f }, { -1.5f, -0.5f }, { -1.75f, -0.75f }, { -2, -1 },

            // Top Tips:
            { 0, 2 }, { 0, -2 }, { 2, 0 }, { -2, 0 },

            // Half circle 1
            { -0.958f,    2.2f }, { -0.042f,    2.2f }, { -0.264f,  2.440f }, { -0.736f,  2.440f }, { -0.5f,  2.5f },
            { -0.958f,   -2.2f }, { -0.042f,   -2.2f }, { -0.264f, -2.440f }, { -0.736f, -2.440f }, { -0.5f, -2.5f },
            {    2.2f, -0.958f }, {    2.2f, -0.042f }, {  2.440f, -0.264f }, {  2.440f, -0.736f }, {  2.5f, -0.5f },
            {   -2.2f, -0.958f }, {   -2.2f, -0.042f }, { -2.440f, -0.264f }, { -2.440f, -0.736f }, { -2.5f, -0.5f },

            // Half circle 2
            { 0.958f,   2.2f }, { 0.042f,   2.2f }, {  0.264f,  2.440f }, {  0.736f,  2.440f }, {  0.5f,  2.5f },
            { 0.958f,  -2.2f }, { 0.042f,  -2.2f }, {  0.264f, -2.440f }, {  0.736f, -2.440f }, {  0.5f, -2.5f },
            {   2.2f, 0.958f }, {   2.2f, 0.042f }, {  2.440f,  0.264f }, {  2.440f,  0.736f }, {  2.5f,  0.5f },
            {  -2.2f, 0.958f }, {  -2.2f, 0.042f }, { -2.440f,  0.264f }, { -2.440f,  0.736f }, { -2.5f,  0.5f }
    };

    /**
     * @param radius the radius of the circle
     * @param amountPoints the amount of points that make up the circle
     * @param extraY Extra y to add to the locs
     * @return a list of locations that make up a circle
     */
    public static List<Vector3d> getCirclePoints(double extraY, double radius, short amountPoints) {
        List<Vector3d> result = new ArrayList<>();
        for (int i = 0; i < amountPoints; ++i) {
            final double angle = Math.toRadians(((double) i / amountPoints) * 360d);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            result.add(new Vector3d(x, extraY, z));
        }
        return result;
    }

    /**
     * Spawns particles on a circle around a given location.
     * @param loc The location of the centre of the circle.
     * @param extraY Extra y to add to loc (0 means no extra height).
     * @param radius The radius of the circle around loc
     * @param amountPoints The amount of points used on this circle, more means a more accurate circle.
     * @param particle The particle to spawn
     * @param amountParticlesPerPoint Amount of particles to spawn on a point
     */
    static void spawnInCircle(Location loc, double extraY, double radius, short amountPoints, Particle particle, int amountParticlesPerPoint) {
        for (int i = 0; i < amountPoints; ++i) {
            final double angle = Math.toRadians(((double) i / amountPoints) * 360d);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location pointLoc = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + extraY, loc.getZ() + z);
            pointLoc.getWorld().spawnParticle(particle, pointLoc, amountParticlesPerPoint, 0, 0, 0, 0, null, true);
        }
    }


    /**
     * Spawns particles on 4 heart shapes  around a given location.
     * @param loc The location of the centre of the pattern
     * @param extraY Extra y to add to loc (0 means no height).
     * @param particle The particle to spawn
     */
    static void spawn4Hearts(Location loc, double extraY, Particle particle) {
        for (float[] xz : HEART_OFFSETS) {
            loc.getWorld().spawnParticle(particle, new Location(loc.getWorld(),
                    loc.getX() + xz[0],
                    loc.getY() + extraY,
                    loc.getZ() + xz[1]
            ), 3, 0, 0, 0, 0.002, null, true);
        }
    }
}