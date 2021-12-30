package net.forthecrown.structure;

import net.kyori.adventure.key.Keyed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.io.StringReader;

/**
 * A function ran by a structure after being placed
 */
public interface StructureFunction {
    void run(StructurePlaceContext context, CompoundTag placeData);

    interface StructureFunctionType<T extends StructureFunction> extends Keyed {
        T load(Tag tag);
        Tag save(T val);

        T parse(StringReader reader);
    }
}
