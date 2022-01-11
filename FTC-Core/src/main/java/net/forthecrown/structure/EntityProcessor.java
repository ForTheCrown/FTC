package net.forthecrown.structure;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface EntityProcessor {
    @Nullable
    EntityPlaceData process(StructureEntityInfo info, StructurePlaceContext context, @Nullable EntityPlaceData previous);
}
