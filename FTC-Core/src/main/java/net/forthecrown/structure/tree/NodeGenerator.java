package net.forthecrown.structure.tree;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.structure.PlaceMirror;
import net.forthecrown.structure.PlaceRotation;
import net.forthecrown.structure.StructureTransform;
import net.forthecrown.utils.CrownRandom;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.transformation.BoundingBoxes;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;

public class NodeGenerator<T extends StructureNode> {
    private static final Logger LOGGER = Crown.logger();

    private final StructureType<T> type;

    public NodeGenerator(StructureType<T> type) {
        this.type = type;
    }

    public StructureType<T> getType() {
        return type;
    }

    public List<StructureNodeType<? extends T>> getAllPossibleTypes() {
        return type.getAllPossibleTypes();
    }

    public StructureTree<T> generate() {
        LOGGER.info("Starting generation of structure tree, id: {}", type.key());
        StructureTree<T> result = new StructureTree<>();

        result.setStart(createEntry(
                new GenContext(
                        new ObjectArrayList<>(),
                        new CrownRandom(),
                        getType().maxDepth()
                ),
                NodeConnector.EMPTY
        ));

        return result;
    }

    private StructureTree.Entry<T> createEntry(GenContext context, NodeConnector c) {
        StructureNodeType<? extends T> foundType = getApplicable(context);
        if(foundType == null) /*foundType = getType().getEndType(); */ return null;

        StructureTree.Entry<T> result = new StructureTree.Entry<>(foundType.createEmpty());
        PlaceRotation rotation = context.addRot(c.rotation());

        Vector3i effOffset = StructureTransform.DEFAULT.transform(
                Vector3i.ZERO,
                c.offset(),
                foundType.createPivot(),
                PlaceMirror.NONE,
                rotation
        );

        context.addLastPos(effOffset);

        result.getParent().setOffset(effOffset);
        result.getParent().setRotation(rotation);

        addChildrenRecursively(result, context);

        return result;
    }

    private StructureNodeType<? extends T> getApplicable(GenContext context) {
        List<StructureNodeType<? extends T>> possible = new ObjectArrayList<>(getAllPossibleTypes());

        if(context.previousType != null) {
            StructureNodeType<? extends T> prev = context.previousType;
            Iterator<StructureNodeType<? extends T>> iterator = possible.iterator();

            while (iterator.hasNext()) {
                StructureNodeType<T> f = (StructureNodeType<T>) iterator.next();
                //BoundingBox box = f.createBounds(f.createEmpty(), context.getLastPos(), context.getLastRot());

                //if(!context.legalArea(box)) iterator.remove();
                if(!f.canGenerateNextTo(prev)) iterator.remove();
            }
        }

        if(possible.isEmpty()) return null;

        StructureNodeType<T> node = (StructureNodeType<T>) context.random.pickRandomEntry(possible);
        BoundingBox box = node.createBounds(node.createEmpty(), context.getLastPos(), context.getLastRot());

        context.generatedArea.add(box.inflatedBy(-1));

        return node;
    }

    private void addChildrenRecursively(StructureTree.Entry<T> entry, GenContext context) {
        List<NodeConnector> connectors = entry.getParent().getType().getConnectors();
        if(connectors == null || connectors.isEmpty()) return;

        context.previousType = entry.getParent().getType();

        for (NodeConnector c : connectors) {
            if (context.depth >= context.maxDepth) {
                T empty = getType().getEndType().createEmpty();
                c.apply(empty);

                entry.addChild(empty);
                continue;
            }

            StructureTree.Entry<T> e = createEntry(context.copy(), c);
            if(e == null) continue;
            entry.addChild(e);
        }
    }

    private static class GenContext {
        private final List<BoundingBox> generatedArea;

        private PlaceRotation lastRot;
        private Vector3i lastPos;

        private final CrownRandom random;
        private final int maxDepth;
        private int depth;
        private StructureNodeType previousType;

        public GenContext(List<BoundingBox> area, CrownRandom random, int maxDepth) {
            generatedArea = area;
            this.random = random;
            this.maxDepth = maxDepth;
        }

        public PlaceRotation getLastRot() {
            return lastRot == null ? PlaceRotation.D_0 : lastRot;
        }

        public PlaceRotation addRot(PlaceRotation rot) {
            return this.lastRot = getLastRot().add(rot);
        }

        public Vector3i getLastPos() {
            return lastPos == null ? Vector3i.ZERO : lastPos;
        }

        public Vector3i addLastPos(Vector3i pos) {
            return this.lastPos = getLastPos().immutable().add(pos);
        }

        public boolean legalArea(BoundingBox box) {
            for (BoundingBox b: generatedArea) {
                if(BoundingBoxes.overlaps(b, box)) return false;
            }

            return true;
        }

        public GenContext copy() {
            GenContext context = new GenContext(generatedArea, random, maxDepth);
            context.depth = depth + 1;
            context.previousType = previousType;
            context.lastRot = lastRot;

            return context;
        }
    }
}
