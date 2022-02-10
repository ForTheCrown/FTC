package net.forthecrown.structure.tree.test;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Keys;
import net.forthecrown.registry.Registries;
import net.forthecrown.structure.tree.StructureNodeType;
import net.forthecrown.structure.tree.StructureType;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TestStructure implements StructureType<TestNode> {
    public static final TestStructure INSTANCE = new TestStructure();
    private final Key key = Keys.forthecrown("test_structure_type");

    final List<StructureNodeType<? extends TestNode>> types = new ObjectArrayList<>();

    public TestStructure() {
        Registries.STRUCTURE_TYPES.register(key, this);
    }

    @Override
    public List<StructureNodeType<? extends TestNode>> getAllPossibleTypes() {
        return types;
    }

    @Override
    public StructureNodeType<? extends TestNode> getEndType() {
        return TestNodes.END;
    }

    @Override
    public StructureNodeType<? extends TestNode> getStartType() {
        return FtcUtils.RANDOM.pickRandomEntry(getAllPossibleTypes());
    }

    @Override
    public int maxDepth() {
        return 10;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
