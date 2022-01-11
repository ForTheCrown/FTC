package net.forthecrown.structure;

import net.forthecrown.utils.math.Vector3i;
import org.jetbrains.annotations.NotNull;

public interface StructureTransform {
    StructureTransform DEFAULT = (destination, relative, prevResult) -> destination.add(relative);

    @NotNull Vector3i apply(Vector3i destination, Vector3i relative, Vector3i prevResult);
}
