package net.forthecrown.structure;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EmptyEntityProcessor implements EntityProcessor {
    @Override
    public @Nullable EntityPlaceData process(StructureEntityInfo info, StructurePlaceContext context, @Nullable EntityPlaceData previous) {
        if(previous != null) return previous;

        Vec3 abs = context.transform(info.offset());
        return new EntityPlaceData(abs, info.entityData());
    }
}
