package net.forthecrown.utils.math;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.With;
import net.forthecrown.structure.Rotation;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

/**
 * A transformation applied to vectors
 */
@Getter @With
@AllArgsConstructor
@ToString
public class Transform {
    /* ----------------------------- CONSTANTS ------------------------------ */

    /** Transformation which returns the input */
    public static final Transform IDENTITY = new Transform(
            Rotation.NONE,
            Vector3d.ZERO,
            Vector3d.ZERO,
            Vector3d.ONE
    );

    /* ----------------------------- INSTANCE FIELDS ------------------------------ */

    /** Rotation to apply to given vectors */
    private final Rotation rotation;

    /** Pivot position to use for given vectors */
    private final Vector3d pivot;

    /** Offset to apply to given offsets */
    private final Vector3d offset;

    /** Value to multiply the result by, before pivot and offset application */
    private final Vector3d scalar;

    /* ----------------------------- STATIC CONSTRUCTORS ------------------------------ */

    public static Transform offset(Vector3d v) {
        return IDENTITY.withOffset(v);
    }

    public static Transform offset(Vector3i v) {
        return IDENTITY.withOffset(v.toDouble());
    }

    public static Transform rotation(Rotation rotation) {
        return IDENTITY.withRotation(rotation);
    }

    public static Transform scale(Vector3d scalar) {
        return IDENTITY.withScalar(scalar);
    }

    /* ----------------------------- TRANSFORMATION ------------------------------ */

    public Vector3i apply(Vector3i v) {
        return apply(v.toDouble()).toInt();
    }

    public Vector3d apply(Vector3d v) {
        if (isIdentity()) {
            return v;
        }

        var pivoted = v.sub(pivot);

        if (rotation != Rotation.NONE) {
            pivoted = rotation.rotate(pivoted);
        }

        pivoted = pivoted.mul(scalar);

        return offset.add(pivoted.add(pivot));
    }

    public boolean isIdentity() {
        return rotation == Rotation.NONE
                && offset.equals(Vector3d.ZERO)
                && scalar.equals(Vector3d.ZERO);
    }

    /* ----------------------------- MODIFICATION ------------------------------ */

    public Transform addOffset(Vector3i v) {
        return addOffset(v.toDouble());
    }

    public Transform addOffset(Vector3d v) {
        return withOffset(offset.add(v));
    }

    public Transform addPivot(Vector3i v) {
        return addPivot(v.toDouble());
    }

    public Transform addPivot(Vector3d v) {
        return withPivot(pivot.add(v));
    }

    public Transform withIPivot(Vector3i v) {
        return withPivot(v.toDouble());
    }

    public Transform addRotation(Rotation rotation) {
        return withRotation(this.rotation.add(rotation));
    }
}