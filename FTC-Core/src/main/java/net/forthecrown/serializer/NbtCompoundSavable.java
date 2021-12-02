package net.forthecrown.serializer;

import net.minecraft.nbt.CompoundTag;

public interface NbtCompoundSavable {
    /**
     * Saves the object into the given Compound
     * @param into The compound to save into
     */
    void save(CompoundTag into);
}
