package net.forthecrown.serializer;

import net.minecraft.nbt.CompoundTag;

public interface NbtCompoundLoadable {
    void load(CompoundTag tag);
}
