package net.forthecrown.structure;

import java.util.List;

public interface Structure<T extends StructureNode> {
    int maxDepth();

    StructureNodeType<? extends T> endType();
    List<StructureNodeType<? extends T>> allTypes();
}
