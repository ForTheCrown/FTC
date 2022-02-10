package net.forthecrown.structure.tree;

import net.forthecrown.structure.PlaceRotation;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.key.Keyed;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

public interface StructureNodeType<T extends StructureNode> extends Keyed {
    StructureType<T> getStructureType();

    T createEmpty();
    T load(CompoundTag tag);

    boolean canGenerateNextTo(StructureNodeType type);
    List<NodeConnector> getConnectors();

    Vector3i getEntrancePos();
    Vector3i createPivot();

    BoundingBox createBounds(T node, Vector3i placePos, PlaceRotation rotation);
}
