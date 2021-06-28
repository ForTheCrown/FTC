package net.forthecrown.serializer;

import net.minecraft.nbt.CompoundTag;

public interface NbtCompoundSavable {
    CompoundTag save(CompoundTag into);

    default CompoundTag save(){
        return save(new CompoundTag());
    }
}
