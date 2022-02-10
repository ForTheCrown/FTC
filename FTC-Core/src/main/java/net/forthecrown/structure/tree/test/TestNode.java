package net.forthecrown.structure.tree.test;

import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.tree.StructureNode;
import net.forthecrown.structure.tree.StructureNodeType;
import net.forthecrown.structure.tree.TemplateStructureNode;
import net.minecraft.nbt.CompoundTag;

public abstract class TestNode extends TemplateStructureNode {
    public TestNode(StructureNodeType<? extends StructureNode> type, BlockStructure structure) {
        super(type, structure);
    }

    public TestNode(StructureNodeType<? extends StructureNode> type, CompoundTag tag) {
        super(type, tag);
    }
}
