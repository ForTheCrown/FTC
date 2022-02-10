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
                    new NodeConnector(new Vector3i(21, 0, 0), PlaceRotation.D_0)
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
            return new Vector3i(10, 0, 5);
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
            return new Vector3i(5, 0, 5);
        }
    }
    public static class Test3WayType extends TestNodeType<TestNodeImpl> {
        public Test3WayType() {
            super("3_way_type",
                    new NodeConnector(new Vector3i(13, 0, 16), PlaceRotation.D_90),
                    new NodeConnector(new Vector3i(3, 0, 0), PlaceRotation.D_270)
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
            return new Vector3i(0, 0, 4);
        }

        @Override
        public Vector3i createPivot() {
            return new Vector3i(8, 0, 9);
        }
    }

    public static class Test4WayType extends TestNodeType<TestNodeImpl> {
        public Test4WayType() {
            super("test_4_way_type",
                    new NodeConnector(new Vector3i(12, 0, 17), PlaceRotation.D_90),
                    new NodeConnector(new Vector3i(14, 0, 2), PlaceRotation.D_0),
                    new NodeConnector(new Vector3i(2, 0, 0), PlaceRotation.D_270)
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
            return new Vector3i(0, 0, 2);
        }

        @Override
        public Vector3i createPivot() {
            return new Vector3i(7, 0, 7);
        }
    }
}