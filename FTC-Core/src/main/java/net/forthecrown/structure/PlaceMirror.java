package net.forthecrown.structure;

import net.minecraft.world.level.block.Mirror;

public enum PlaceMirror {
    // DO NOT CHANGE ORDER OF VALUES
    // toVanilla method requires these
    // to not change
    NONE,   // Vanilla: NONE
    Z_AXIS, // Vanilla: LEFT_RIGHT
    X_AXIS; // Vanilla: FRONT_BACK

    public Mirror toVanilla() {
        return Mirror.values()[ordinal()];
    }
}
