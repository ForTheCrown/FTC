package net.forthecrown.serializer;

import net.minecraft.nbt.CompoundTag;

public interface NbtCompoundSavable {
    /**
     * Saves the object into the given Compound
     * @param into The compound to save into
     * @return The NBT representation of the object in the compound
     */
    CompoundTag save(CompoundTag into);

    /**
     * Saves the object into an empty Compound tag
     * @return The saved object
     */
    default CompoundTag save(){
        return save(new CompoundTag());
    }
}
