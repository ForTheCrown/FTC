package net.forthecrown.serializer;

import net.minecraft.nbt.Tag;

public interface NbtSerializable {
    Tag saveAsTag();
}
