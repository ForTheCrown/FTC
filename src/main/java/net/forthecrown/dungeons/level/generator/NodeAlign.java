package net.forthecrown.dungeons.level.generator;

import lombok.experimental.UtilityClass;
import net.forthecrown.dungeons.level.DungeonPiece;
import net.forthecrown.dungeons.level.gate.AbsoluteGateData;
import net.forthecrown.dungeons.level.gate.DungeonGate;
import net.forthecrown.dungeons.level.gate.GateData;
import net.forthecrown.structure.Rotation;
import net.forthecrown.utils.math.Transform;
import net.forthecrown.utils.math.Bounds3i;
import org.spongepowered.math.vector.Vector3i;

public @UtilityClass class NodeAlign {
    public AbsoluteGateData align(DungeonPiece created, AbsoluteGateData exit, GateData entrance) {
        Vector3i exitRight = exit.rightSide();

        // Derive rotation from direction difference between entrance and exit
        // and subtract 180 degrees, so they'd face each other when rotated
        Rotation rotation = entrance.direction().deriveRotationFrom(exit.direction())
                .add(Rotation.CLOCKWISE_180);

        // Get the relative entrance origin point
        Vector3i relativeEntranceOrigin = entrance.direction()
                .left()
                .getMod()
                .mul(entrance.opening().width(), 0, entrance.opening().width())
                .div(2);

        // Turn the entrance-center relative entrance origin location into
        // a structure-origin relative location
        Vector3i entranceLeft = entrance.parentOffset()
                .add(relativeEntranceOrigin);

        // Apply rotations to both structure and
        // entrance offset
        if (rotation != Rotation.NONE) {
            created.apply(Transform.rotation(rotation));
            entranceLeft = rotation.rotate(entranceLeft);
        }

        // Move the left side of the entrance to the
        // exit's right side
        created.apply(
                Transform.offset(exitRight.sub(entranceLeft))
                        .withIPivot(created.getPivotPosition())
        );

        if (created instanceof DungeonGate gate) {
            gate.setParentExit(exit);
            gate.setOriginGate(entrance.toAbsolute(gate));

            return gate.getOriginGate();
        }

        return entrance.toAbsolute(created);
    }

    public Vector3i pivotPoint(Bounds3i bb, Rotation rotation) {
        var min = bb.min();
        var max = bb.max();

        return switch (rotation) {
            case NONE -> min;
            case CLOCKWISE_90 -> min.withX(max.x());
            case COUNTERCLOCKWISE_90 -> min.withZ(max.z());
            case CLOCKWISE_180 -> max.withY(min.y());
        };
    }
}