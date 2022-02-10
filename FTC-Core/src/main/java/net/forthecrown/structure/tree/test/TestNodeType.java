package net.forthecrown.structure.tree.test;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Keys;
import net.forthecrown.registry.Registries;
import net.forthecrown.structure.PlaceMirror;
import net.forthecrown.structure.PlaceRotation;
import net.forthecrown.structure.StructureTransform;
import net.forthecrown.structure.tree.NodeConnector;
import net.forthecrown.structure.tree.StructureNodeType;
import net.forthecrown.structure.tree.StructureType;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.key.Key;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public abstract class TestNodeType<T extends TestNode> implements StructureNodeType<T> {
    private final Key key;
    private final List<NodeConnector> connectors;

    public TestNodeType(String val, NodeConnector... connectors) {
        this.key = Keys.parse("node_types/" + val);
        this.connectors = connectors == null ? new ObjectArrayList<>() : Arrays.asList(connectors);

        Registries.STRUCTURE_NODE_TYPES.register(key, this);
    }

    @Override
    public StructureType<T> getStructureType() {
        return (StructureType<T>) TestStructure.INSTANCE;
    }

    @Override
    public boolean canGenerateNextTo(StructureNodeType type) {
        return true;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @Override
    public List<NodeConnector> getConnectors() {
        return connectors;
    }

    @Override
    public BoundingBox createBounds(T node, Vector3i placePos, PlaceRotation rotation) {
        Vector3i min = placePos;
        Vector3i max = StructureTransform.DEFAULT.transform(
                placePos,
                node.getStructure().getSize(),
                createPivot(),
                PlaceMirror.NONE,
                rotation
        );

        min = min.getMinimum(max);
        max = min.getMaximum(max);

        return new BoundingBox(
                min.getX(), min.getY(), min.getZ(),
                max.getX(), max.getY(), max.getZ()
        );
    }
}
