package net.forthecrown.structure.tree.test;

import net.forthecrown.core.Crown;
import net.forthecrown.core.Keys;
import net.forthecrown.registry.Registries;
import net.forthecrown.structure.BlockStructure;
import net.forthecrown.structure.PlaceRotation;
import net.forthecrown.structure.StructureTransform;
import net.forthecrown.structure.tree.*;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.key.Key;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class TestNodes {
    private TestNodes() {}

    public static final Key END_STRUCT = Keys.forthecrown("test_end_struct");
    public static final Key HALLWAY_STRUCT = Keys.forthecrown("test_hallway_struct");
    public static final Key THREE_WAY_STRUCT = Keys.forthecrown("test_3_way_struct");
    public static final Key FOUR_WAY_STRUCT = Keys.forthecrown("test_4_way_struct");

    public static final TestHallwayType HALLWAY = new TestHallwayType();
    public static final TestEndType END = new TestEndType();
    public static final Test3WayType THREE_WAY = new Test3WayType();
    public static final Test4WayType FOUR_WAY = new Test4WayType();

    public static void init() {
        TestStructure.INSTANCE.types.add(HALLWAY);
        TestStructure.INSTANCE.types.add(END);
        TestStructure.INSTANCE.types.add(THREE_WAY);
        TestStructure.INSTANCE.types.add(FOUR_WAY);

        Crown.logger().info("TestNodes initialized");
    }

    public static StructureTree<TestNode> createDoubleHallway() {
        StructureTree<TestNode> nodes = new StructureTree<>();
        StructureTree.Entry<TestNode> parent = new StructureTree.Entry<>(HALLWAY.createEmpty());

        TestNode node = HALLWAY.createEmpty();
        HALLWAY.getConnectors().get(0).apply(node);

        TestNode hallway2 = HALLWAY.createEmpty();
        hallway2.setOffset(Vector3i.ZERO);
        hallway2.setRotation(PlaceRotation.D_180);

        parent.addChild(hallway2);
        parent.addChild(node);

        nodes.setStart(parent);

        return nodes;
    }

    public static StructureTree<TestNode> createTestTree() {
        StructureTree<TestNode> testTree = new StructureTree<>();
        StructureTree.Entry<TestNode> parent = new StructureTree.Entry<>(THREE_WAY.createEmpty());
        parent.getParent().setRotation(PlaceRotation.D_90);
        addEnd(parent);

        /*for (NodeConnector c: FOUR_WAY.getConnectors()) {
            TestNode node = HALLWAY.createEmpty();
            c.apply(node);

            StructureTree.Entry<TestNode> entry = new StructureTree.Entry<>(node);
            addEnd(entry);

            parent.addChild(entry);
        }

        TestNode node = HALLWAY.createEmpty();
        node.setRotation(PlaceRotation.D_180);
        node.setOffset(new Vector3i(0, 0, 2));

        StructureTree.Entry<TestNode> entry = new StructureTree.Entry<>(node);
        addEnd(entry);

        parent.addChild(entry);*/

        testTree.setStart(parent);
        return testTree;
    }

    private static void addEnd(StructureTree.Entry<TestNode> entry) {
        TestNode node = entry.getParent();
        List<NodeConnector> connectors = node.getType().getConnectors();

        for (NodeConnector c: connectors) {
            TestNode n = END.createEmpty();
            PlaceRotation rotation = node.getRotation().add(c.rotation());

            n.setOffset(c.offset(rotation));
            n.setRotation(rotation);

            entry.addChild(n);
        }
    }

    public static void generateAndPlace(World world, Vector3i destination) {
        NodeGenerator<TestNode> generator = new NodeGenerator<>(TestStructure.INSTANCE);

        NodePlaceContext context = new NodePlaceContext(world, StructureTransform.DEFAULT, destination, PlaceRotation.D_0);
        StructureTree<TestNode> tree = generator.generate();

        Crown.logger().info("Generated tree info:");
        Crown.logger().info("size: {}", tree.size());
        Crown.logger().info("------------");

        tree.place(context);

        Crown.logger().info("Writing test file");
        writeTestFile(tree);
    }

    private static void writeTestFile(StructureTree<TestNode> tree) {
        File file = new File(Crown.dataFolder(), "struct_node_output.dat");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        CompoundTag written = tree.save();

        try {
            NbtIo.write(written, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class TestNodeImpl extends TestNode {
        public TestNodeImpl(StructureNodeType<? extends StructureNode> type, BlockStructure structure) {
            super(type, structure);
        }

        public TestNodeImpl(StructureNodeType<? extends StructureNode> type, CompoundTag tag) {
            super(type, tag);
        }

        @Override
        public TestNodeType<? extends TestNodeImpl> getType() {
            return (TestNodeType<? extends TestNodeImpl>) super.getType();
        }
    }

    public static class TestHallwayType extends TestNodeType<TestNodeImpl> {
        public TestHallwayType() {
            super("test_hallway",
                    new NodeConnector(new Vector3i(22, 0, 0), PlaceRotation.D_0)
            );
        }

        @Override
        public TestNodeImpl createEmpty() {
            return new TestNodeImpl(this, Registries.STRUCTURES.get(HALLWAY_STRUCT));
        }

        @Override
        public TestNodeImpl load(CompoundTag tag) {
            return new TestNodeImpl(this, tag);
        }

        @Override
        public Vector3i getEntrancePos() {
            return Vector3i.ZERO;
        }

        @Override
        public Vector3i createPivot() {
            return new Vector3i(10, 0, 6);
        }
    }
    public static class TestEndType extends TestNodeType<TestNodeImpl> {
        public TestEndType() {
            super("test_end");
        }

        @Override
        public TestNodeImpl createEmpty() {
            return new TestNodeImpl(this, Registries.STRUCTURES.get(END_STRUCT));
        }

        @Override
        public TestNodeImpl load(CompoundTag tag) {
            return new TestNodeImpl(this, tag);
        }

        @Override
        public Vector3i getEntrancePos() {
            return Vector3i.ZERO;
        }

        @Override
        public Vector3i createPivot() {
            return new Vector3i(6, 0, 6);
        }
    }
    public static class Test3WayType extends TestNodeType<TestNodeImpl> {
        public Test3WayType() {
            super("3_way_type",
                    new NodeConnector(new Vector3i(13, 0, 17), PlaceRotation.D_90),
                    new NodeConnector(new Vector3i(3, 0, -1), PlaceRotation.D_270)
            );
        }

        @Override
        public TestNodeImpl createEmpty() {
            return new TestNodeImpl(this, Registries.STRUCTURES.get(THREE_WAY_STRUCT));
        }

        @Override
        public TestNodeImpl load(CompoundTag tag) {
            return new TestNodeImpl(this, tag);
        }

        @Override
        public Vector3i getEntrancePos() {
            return new Vector3i(0, 0, 5);
        }

        @Override
        public Vector3i createPivot() {
            return new Vector3i(9, 0, 10);
        }
    }

    public static class Test4WayType extends TestNodeType<TestNodeImpl> {
        public Test4WayType() {
            super("test_4_way_type",
                    new NodeConnector(new Vector3i(12, 0, 18), PlaceRotation.D_90),
                    new NodeConnector(new Vector3i(15, 0, 2), PlaceRotation.D_0),
                    new NodeConnector(new Vector3i(2, 0, -1), PlaceRotation.D_270)
            );
        }

        @Override
        public TestNodeImpl createEmpty() {
            return new TestNodeImpl(this, Registries.STRUCTURES.get(FOUR_WAY_STRUCT));
        }

        @Override
        public TestNodeImpl load(CompoundTag tag) {
            return new TestNodeImpl(this, tag);
        }

        @Override
        public Vector3i getEntrancePos() {
            return new Vector3i(0, 0, 3);
        }

        @Override
        public Vector3i createPivot() {
            return new Vector3i(8, 0, 8);
        }
    }
}