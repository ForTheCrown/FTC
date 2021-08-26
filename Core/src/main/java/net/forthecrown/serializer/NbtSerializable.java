package net.forthecrown.serializer;

import net.minecraft.nbt.Tag;

public interface NbtSerializable {

    /**
     * Saves the object as an NBT tag
     * @return the nbt tag representation of this object
     */
    Tag saveAsTag();
}
