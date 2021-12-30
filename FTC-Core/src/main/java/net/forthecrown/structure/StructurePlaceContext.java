package net.forthecrown.structure;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.utils.BlockPlacer;
import net.forthecrown.utils.math.Vector3i;

import java.util.List;

/**
 * The context of a structure being placed
 */
public class StructurePlaceContext {
    private final BlockStructure structure;
    private final List<BlockProcessor> processors = new ObjectArrayList<>();
    private final Vector3i destination;
    private final BlockPlacer placer;
    private PlaceRotation rotation;
    private Vector3i pivot;

    public StructurePlaceContext(BlockStructure structure, Vector3i destination, BlockPlacer placer) {
        this.structure = structure;
        this.destination = destination;
        this.placer = placer;
        this.pivot = Vector3i.ZERO;
    }

    public BlockStructure getStructure() {
        return structure;
    }

    public Vector3i getDestination() {
        return destination.clone();
    }

    /**
     * Takes the relative offset and returns an absolute
     * vector coordinate.
     * @param relative The relative coordinate
     * @return The absolute coordinate
     */
    public Vector3i toAbsolute(Vector3i relative) {
        // TODO This is where rotations and any other
        // potential position modifers should be applied
        return getDestination().add(relative);
    }

    public StructurePlaceContext setRotation(PlaceRotation rotation) {
        this.rotation = rotation;
        return this;
    }

    public PlaceRotation getRotation() {
        return rotation;
    }

    public StructurePlaceContext addProccessor(BlockProcessor processor) {
        this.processors.add(processor);
        return this;
    }

    public StructurePlaceContext addEmptyProcessor() {
        return addProccessor(new EmptyBlockProcessor());
    }

    public StructurePlaceContext setPivot(Vector3i pivot) {
        this.pivot = pivot;
        return this;
    }

    public Vector3i getPivot() {
        return pivot;
    }

    public Vector3i getAbsolutePivot() {
        return pivot.clone().add(getDestination());
    }

    public BlockPlacer getPlacer() {
        return placer;
    }

    BlockPlaceData runProccessors(BlockPalette palette, BlockPalette.StateData data) {
        if(processors.isEmpty()) return null;

        BlockPlaceData result = null;
        for (BlockProcessor p: processors) {
            result = p.process(palette, data, this, result);
        }

        return result;
    }
}
