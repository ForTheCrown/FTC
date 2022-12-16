package net.forthecrown.utils.math;

import org.bukkit.util.BoundingBox;
import org.junit.jupiter.api.Test;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractBounds3iTest {
    static final float PLR_HEIGHT = 1.8F;
    static final float PLR_WIDTH = 0.6F / 2;

    static final Bounds3i bounds3i = Bounds3i.of(
            Vector3i.from(186, 64, 224),
            Vector3i.from(190, 69, 228)
    );

    @Test
    void contains() {
        Vector3i yes1 = Vector3i.from(190, 66, 226);
        Vector3i no1 = Vector3i.from(191, 66, 226);

        Vector3i yes2 = Vector3i.from(188, 66, 228);
        Vector3i no2 = Vector3i.from(188, 66, 229);

        Vector3i yes3 = Vector3i.from(188, 65, 224);
        Vector3i no3 = Vector3i.from(188, 65, 220);

        assertTrue(bounds3i.contains(yes1));
        assertTrue(bounds3i.contains(yes2));
        assertTrue(bounds3i.contains(yes3));

        assertFalse(bounds3i.contains(no1));
        assertFalse(bounds3i.contains(no2));
        assertFalse(bounds3i.contains(no3));
    }

    @Test
    void overlaps() {
        double[][] overlapping = {
                { 190.700, 65, 228.700 },
                { 186.300, 65, 224.300 },
                { 189.300, 65, 227.300 },
                { 188.420, 68, 226.466 },
        };

        int index = 0;
        for (double[] rYes: overlapping) {
            Vector3d vec = Vector3d.from(rYes[0], rYes[1], rYes[2]);
            var bounds = bound(vec);

            assertTrue(
                    bounds3i.overlaps(bounds),

                    String.format("%s) SHOULD overlap, pos=%s\n",
                            index, pos(bounds)
                    )
            );

            index++;
        }
    }

    @Test
    void doesNotOverlap() {
        var bounds3i = AbstractBounds3iTest.bounds3i.clone();

        double[][] notOverlapping = {
                { 185.700, 65, 223.700 },
                { 191.300, 65, 223.700 },
                { 185.700, 65, 229.300 },
                { 189.300, 65, 229.300 },
        };

        int index = 0;
        for (var arr: notOverlapping) {
            Vector3d vec = Vector3d.from(arr[0], arr[1], arr[2]);
            var bounds = bound(vec);

            assertFalse(
                    bounds3i.overlaps(bounds),
                    String.format("%s) should NOT overlap, pos=%s\n",
                            index, pos(bounds)
                    )
            );
            index++;
        }
    }

    static String pos(BoundingBox box) {
        return String.format("%.2f %.2f %.2f",
                box.getCenterX(), box.getMinY(), box.getCenterZ()
        );
    }

    static BoundingBox bound(Vector3d pos) {
        return new BoundingBox(
                rnd(pos.x() - PLR_WIDTH), rnd(pos.y()             ),  rnd(pos.z() - PLR_WIDTH),
                rnd(pos.x() + PLR_WIDTH), rnd(pos.y() + PLR_HEIGHT), rnd(pos.z() + PLR_WIDTH)
        );
    }

    private static double rnd(double v) {
        return GenericMath.round(v, 5);
    }
}