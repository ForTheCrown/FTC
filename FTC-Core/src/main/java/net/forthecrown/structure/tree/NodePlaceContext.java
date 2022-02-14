package net.forthecrown.structure.tree;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.core.Crown;
import net.forthecrown.structure.PlaceRotation;
import net.forthecrown.structure.StructureTransform;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.transformation.BoundingBoxes;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.bukkit.World;

import java.util.List;

public class NodePlaceContext {
    private final List<BoundingBox> generatedArea;

    private final World world;
    private final StructureTransform transform;
    private final Vector3i placePos;
    private final PlaceRotation startRotation;

    private int depth;
    private Vector3i offset;

    public NodePlaceContext(World world, StructureTransform transform, Vector3i placePos, PlaceRotation startRotation) {
        this(new ObjectArrayList<>(), world, transform, placePos, startRotation);
    }

    private NodePlaceContext(List<BoundingBox> area, World world, StructureTransform transform, Vector3i placePos, PlaceRotation startRotation) {
        generatedArea = area;
        this.world = world;
        this.transform = transform;
        this.placePos = placePos;
        this.startRotation = startRotation;

        this.offset = Vector3i.ZERO;
        this.depth = 0;
    }

    public List<BoundingBox> getGeneratedArea() {
        return generatedArea;
    }

    public boolean isLegalArea(BoundingBox box) {
        for (BoundingBox b: getGeneratedArea()) {
            if(BoundingBoxes.overlaps(b, box)) return false;
        }

        return true;
    }

    public void addGeneratedArea(BoundingBox box) {
        generatedArea.add(box);
    }

    public StructureTransform getTransform() {
        return transform;
    }

    public Vector3i getPlacePos() {
        return placePos;
    }

    public World getWorld() {
        return world;
    }

    public PlaceRotation getStartRotation() {
        return startRotation;
    }

    public Vector3i getEffectivePlacePos() {
        return getOffset().immutable().add(getPlacePos());
    }

    public Vector3i getOffset() {
        return offset;
    }

    public void setOffset(Vector3i offset) {
        this.offset = offset;
    }

    private static final Logger LOGGER = Crown.logger();

    public void addOffset(Vector3i offset) {
        LOGGER.info("addOffset called, addition: {}, current: {}, caller: {}", offset, getOffset(), StackLocatorUtil.getCallerClass(2));

        setOffset(getOffset().add(offset));

        LOGGER.info("new offset: {}", getOffset());
    }

    public int getDepth() {
        return depth;
    }

    public NodePlaceContext copy() {
        NodePlaceContext context = new NodePlaceContext(generatedArea, world, transform, placePos, startRotation);

        context.depth = depth + 1;

        context.setOffset(getOffset());

        return context;
    }
}
