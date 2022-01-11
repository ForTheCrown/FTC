package net.forthecrown.structure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public record EntityPlaceData(Vec3 absolute, CompoundTag data) {
}
