package net.forthecrown.structure;

import net.minecraft.world.level.block.Mirror;

public enum PlaceMirror {
    NONE,
    LEFT_RIGHT,
    FRONT_BACK;

    public Mirror toVanilla() {
        return Mirror.valueOf(name());
    }
}
