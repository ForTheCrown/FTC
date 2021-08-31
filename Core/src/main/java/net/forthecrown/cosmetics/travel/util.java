package net.forthecrown.cosmetics.travel;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class util {

    /**
     * @param world the world of the locations
     * @param radius the radius of the circle
     * @param amountPoints the amount of points that make up the circle
     * @param extraY Extra y to add to the locs
     * @return a list of locations that make up a circle
     */
    static List<Location> getOnCircle(World world, double extraY, double radius, short amountPoints) {
        List<Location> result = new ArrayList<>();
        for (int i = 0; i < amountPoints; ++i) {
            final double angle = Math.toRadians(((double) i / amountPoints) * 360d);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            result.add(new Location(world, x, extraY, z));
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
    static void spawnOnCircle(Location loc, double extraY, double radius, short amountPoints, Particle particle, int amountParticlesPerPoint) {
        for (int i = 0; i < amountPoints; ++i) {
            final double angle = Math.toRadians(((double) i / amountPoints) * 360d);

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location pointLoc = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + extraY, loc.getZ() + z);
            pointLoc.getWorld().spawnParticle(particle, pointLoc, amountParticlesPerPoint, 0, 0, 0, 0);
        }
    }


    /**
     * Spawns particles on 4 heart shapes  around a given location.
     * @param loc The location of the centre of the pattern
     * @param extraY Extra y to add to loc (0 means no height).
     * @param particle The particle to spawn
     */
    static void spawnOnHearts(Location loc, double extraY, Particle particle) {
        double[][] locations = {
                // Bottom Tips:
                {0, 1}, {0, -1}, {1, 0}, {-1, 0},

                // Right straights:
                {0.25, 1.25}, {0.5, 1.5}, {0.75, 1.75}, {1, 2},
                {0.25, -1.25}, {0.5, -1.5}, {0.75, -1.75}, {1, -2},
                {1.25, 0.25}, {1.5, 0.5}, {1.75, 0.75}, {2, 1},
                {-1.25, 0.25}, {-1.5, 0.5}, {-1.75, 0.75}, {-2, 1},

                // Left straights:
                {-0.25, 1.25}, {-0.5, 1.5}, {-0.75, 1.75}, {-1, 2},
                {-0.25, -1.25}, {-0.5, -1.5}, {-0.75, -1.75}, {-1, -2},
                {1.25, -0.25}, {1.5, -0.5}, {1.75, -0.75}, {2, -1},
                {-1.25, -0.25}, {-1.5, -0.5}, {-1.75, -0.75}, {-2, -1},

                // Top Tips:
                {0, 2}, {0, -2}, {2, 0}, {-2, 0},

                // Half circle 1
                {-0.958, 2.2}, {-0.042, 2.2}, {-0.264, 2.440}, {-0.736, 2.440}, {-0.5, 2.5},
                {-0.958, -2.2}, {-0.042, -2.2}, {-0.264, -2.440}, {-0.736, -2.440}, {-0.5, -2.5},
                {2.2, -0.958}, {2.2, -0.042}, {2.440, -0.264}, {2.440, -0.736}, {2.5, -0.5},
                {-2.2, -0.958}, {-2.2, -0.042}, {-2.440, -0.264}, {-2.440, -0.736}, {-2.5, -0.5},

                // Half circle 2
                {0.958, 2.2}, {0.042, 2.2}, {0.264, 2.440}, {0.736, 2.440}, {0.5, 2.5},
                {0.958, -2.2}, {0.042, -2.2}, {0.264, -2.440}, {0.736, -2.440}, {0.5, -2.5},
                {2.2, 0.958}, {2.2, 0.042}, {2.440, 0.264}, {2.440, 0.736}, {2.5, 0.5},
                {-2.2, 0.958}, {-2.2, 0.042}, {-2.440, 0.264}, {-2.440, 0.736}, {-2.5, 0.5}
        };

        for (double[] xz : locations) {
            loc.getWorld().spawnParticle(particle, new Location(loc.getWorld(),
                    loc.getX() + xz[0],
                    loc.getY() + extraY,
                    loc.getZ() + xz[1]
            ), 3, 0, 0, 0, 0.002);
        }
    }
}
