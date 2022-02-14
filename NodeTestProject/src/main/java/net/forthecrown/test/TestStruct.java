package net.forthecrown.test;

import net.forthecrown.structure.Structure;
import net.forthecrown.structure.StructureNodeType;
import net.forthecrown.structure.TemplateNode;
import net.forthecrown.structure.Templates;

import java.util.ArrayList;
import java.util.List;

public class TestStruct implements Structure<TemplateNode> {
    public static final TestStruct INSTANCE = new TestStruct();

    public final List<StructureNodeType<? extends TemplateNode>> types = new ArrayList<>();

    @Override
    public int maxDepth() {
        return 32;
    }

    @Override
    public StructureNodeType<? extends TemplateNode> endType() {
        return TestNodeTypes.END;
    }

    @Override
    public List<StructureNodeType<? extends TemplateNode>> allTypes() {
        return types;
    }
}
