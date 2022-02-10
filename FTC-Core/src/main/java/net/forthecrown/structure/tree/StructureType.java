package net.forthecrown.structure.tree;

import net.kyori.adventure.key.Keyed;

import java.util.List;

public interface StructureType<T extends StructureNode> extends Keyed {
    List<StructureNodeType<? extends T>> getAllPossibleTypes();

    StructureNodeType<? extends T> getEndType();
    StructureNodeType<? extends T> getStartType();

    int maxDepth();
}
