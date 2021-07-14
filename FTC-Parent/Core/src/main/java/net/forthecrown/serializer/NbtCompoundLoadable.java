package net.forthecrown.serializer;

import net.minecraft.nbt.CompoundTag;

public interface NbtCompoundLoadable {
    /**
     * Loads the object from the given CompoundTag
     * @param tag the tag to load from
     */
    void load(CompoundTag tag);
}
