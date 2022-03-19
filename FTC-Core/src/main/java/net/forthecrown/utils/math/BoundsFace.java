package net.forthecrown.utils.math;

import net.minecraft.core.Direction;
import org.apache.commons.lang.Validate;

public class BoundsFace {
    private final Vector3i min, max;
    private final Direction direction;

    public BoundsFace(Vector3i min, Vector3i max, Direction direction) {
        this.min = min.immutable();
        this.max = max.immutable();
        this.direction = direction;

        int first = direction.getAxis().choose(min.getX(), min.getY(), min.getZ());
        int second = direction.getAxis().choose(max.getX(), max.getY(), max.getZ());

        Validate.isTrue(first == second, "Bounds " + direction + " face does not have same " + direction.getAxis() + " cord");
    }

    public Direction getDirection() {
        return direction;
    }

    public Vector3i getMax() {
        return max;
    }

    public Vector3i getMin() {
        return min;
    }
}
