package net.forthecrown.utils;

import com.destroystokyo.paper.ParticleBuilder;
import lombok.experimental.UtilityClass;
import net.forthecrown.utils.math.AbstractBounds3i;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.spongepowered.math.vector.Vector3d;

public @UtilityClass class Particles {
    public void line(Vector3d dir,
                     Vector3d start,
                     double length,
                     double dist,
                     World world,
                     ParticleBuilder builder
    ) {
        dir = dir.normalize();

        for (double d = 0; d <= length; d += dist) {
            var pos = start.add(dir.mul(d));

            builder.location(world, pos.x(), pos.y(), pos.z())
                    .spawn();
        }
    }

    public void line(Vector3d origin, Vector3d target, double dist, World world, ParticleBuilder builder) {
        Vector3d dif = target.sub(origin);
        double length = dif.length();
        line(dif, origin, length, dist, world, builder);
    }

    public void shape(double dist, World world, ParticleBuilder builder, boolean connectLastToFirst, Vector3d... points) {
        for (int i = 0; i < points.length; i++) {
            Vector3d start = points[i];

            if (i == points.length - 1 && !connectLastToFirst) {
                return;
            }

            Vector3d end = points[(i + 1) % points.length];
            line(start, end, dist, world, builder);
        }
    }

    static void line(World w, Vector3d start, Vector3d end, ParticleBuilder builder) {
        line(start, end, 0.5, w, builder);
    }

    public static void drawBounds(World w, AbstractBounds3i bounds, Color color) {
        var min = bounds.min().toDouble();
        var max = bounds.max().toDouble().add(Vector3d.ONE);

        drawBounds(min, max, w, color);
    }

    public static void drawBounds(Vector3d min, Vector3d max, World w, Color color) {
        var builder = Particle.REDSTONE.builder()
                .data(new Particle.DustOptions(color, 1.5F));

        drawBounds(min, max, w, builder);
    }

    public static void drawBounds(World w, AbstractBounds3i bounds, ParticleBuilder builder) {
        var min = bounds.min().toDouble();
        var max = bounds.max().toDouble().add(Vector3d.ONE);

        drawBounds(min, max, w, builder);
    }

    public static void drawBounds(Vector3d min, Vector3d max, World w, ParticleBuilder builder) {
        Vector3d[] points = {
                min,
                min.withX(max.x()),
                min.withZ(max.z()),
                max.withY(min.y()),

                max,
                max.withX(min.x()),
                max.withZ(min.z()),
                min.withY(max.y())
        };

        // Bottom
        line(w, points[0], points[1], builder);
        line(w, points[0], points[2], builder);
        line(w, points[1], points[3], builder);
        line(w, points[2], points[3], builder);

        if (min.y() != max.y()) {
            // Top
            line(w, points[4], points[5], builder);
            line(w, points[4], points[6], builder);
            line(w, points[5], points[7], builder);
            line(w, points[6], points[7], builder);

            // Sides
            line(w, points[0], points[7], builder);
            line(w, points[1], points[6], builder);
            line(w, points[2], points[5], builder);
            line(w, points[3], points[4], builder);
        }
    }
}