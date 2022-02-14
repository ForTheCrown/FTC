package net.forthecrown.structure;

import net.forthecrown.math.Bounds2i;
import net.forthecrown.math.Rot;
import net.forthecrown.math.Transform;
import net.forthecrown.math.Vec2i;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.forthecrown.Main.LOGGER;

public class TreeGenerator<T extends StructureNode> {
    private static final Random RANDOM = new Random();
    private final Structure<T> structure;

    public TreeGenerator(Structure<T> structure) {
        this.structure = structure;
    }

    public Structure<T> getStructure() {
        return structure;
    }

    public NodeTree<T> create() {
        NodeTree<T> tree = new NodeTree<>();

        GenContext.maxDepth = structure.maxDepth() * structure.allTypes().size();

        tree.setStart(
                createEntry(new GenContext(new ArrayList<>()), Connector.NULL)
        );

        return tree;
    }

    private NodeTree.Entry<T> createEntry(GenContext context, Connector connector) {
        if(GenContext.depth > GenContext.maxDepth) {
            return null;
        }

        LOGGER.info("createEntry called");

        StructureNodeType<T> type = getApplicable(context);

        Vec2i entrance = Transform.DEFAULT.transform(type.entrancePos(), context.getLastRot());
        Vec2i offset = Transform.DEFAULT.transform(connector.pos(), context.getLastRot())
                .subtract(entrance);

        context.addRot(connector.rot());

        T node = type.create();
        node.setOffset(offset);
        node.setRotation(context.getLastRot());
        node.setPivot(entrance);

        NodeTree.Entry<T> entry = new NodeTree.Entry<>(node);
        addChildren(entry, context);

        return entry;
    }

    private StructureNodeType<T> getApplicable(GenContext context) {
        return (StructureNodeType<T>) structure.allTypes().get(RANDOM.nextInt(structure.allTypes().size()));
    }

    private void addChildren(NodeTree.Entry<T> entry, GenContext context) {
        if(GenContext.depth > GenContext.maxDepth) return;
        LOGGER.info("addChildren called, depth {}, max {}", GenContext.depth, GenContext.maxDepth);

        List<Connector> connectors = entry.getParent().getType().connectors();
        if(connectors == null || connectors.isEmpty()) return;

        for (Connector c: connectors) {
            if(GenContext.depth >= GenContext.maxDepth) {
                return;
            }

            NodeTree.Entry<T> e = createEntry(context.copy(), c);
            if(e == null) return;

            entry.addChild(e);
        }
    }

    private static class GenContext {
        private final List<Bounds2i> generatedArea;

        private Rot lastRot;
        private Vec2i lastPos;

        private static int depth;
        private static int maxDepth;
        private StructureNodeType previousType;

        public GenContext(List<Bounds2i> area) {
            generatedArea = area;
        }

        public Rot getLastRot() {
            return lastRot == null ? Rot.D_0 : lastRot;
        }

        public Rot addRot(Rot rot) {
            return this.lastRot = getLastRot().add(rot);
        }

        public Vec2i getLastPos() {
            return lastPos == null ? Vec2i.ZERO : lastPos;
        }

        public Vec2i addLastPos(Vec2i pos) {
            return this.lastPos = getLastPos().add(pos);
        }

        public boolean legalArea(Bounds2i box) {
            for (Bounds2i b: generatedArea) {
                if(b.overlaps(box)) return false;
            }

            return true;
        }

        public GenContext copy() {
            GenContext context = new GenContext(generatedArea);
            depth++;
            context.previousType = previousType;
            context.lastRot = lastRot;

            return context;
        }
    }
}
