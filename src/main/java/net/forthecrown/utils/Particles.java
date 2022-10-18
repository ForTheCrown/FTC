package net.forthecrown.utils;

import com.destroystokyo.paper.ParticleBuilder;
import lombok.experimental.UtilityClass;
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
}