package net.forthecrown.structure;

import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.level.block.Mirror;

public enum PlaceMirror implements Transformer {
    // DO NOT CHANGE ORDER OF VALUES
    // toVanilla method requires these
    // to not change
    NONE,   // Vanilla: NONE
    Z_AXIS, // Vanilla: LEFT_RIGHT
    X_AXIS; // Vanilla: FRONT_BACK

    public Mirror toVanilla() {
        return Mirror.values()[ordinal()];
    }

    @Override
    public Vector3i transform(Vector3i pos) {
        pos = pos.immutable();

        return switch (this) {
            case NONE -> pos;
            case X_AXIS -> pos.setX(-pos.getX());
            case Z_AXIS -> pos.setZ(-pos.getZ());
        };
    }
}
