package net.forthecrown.structure;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.forthecrown.utils.math.Vector3i;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The context of a structure being placed
 */
public class StructurePlaceContext {
    private final BlockStructure structure;
    private final List<BlockProcessor> processors = new ObjectArrayList<>();
    private final List<EntityProcessor> entityProcessors = new ObjectArrayList<>();
    private final Vector3i destination;
    private final BlockPlacer placer;

    private EntityPlacer entityPlacer;
    private StructureTransform transform;
    private PlaceRotation rotation;
    private PlaceMirror mirror;
    private Vector3i pivot;
    private boolean placeEntities;

    public StructurePlaceContext(BlockStructure structure, Vector3i destination, BlockPlacer placer) {
        this.structure = structure;
        this.destination = destination;
        this.placer = placer;

        this.pivot = Vector3i.ZERO;
        this.rotation = PlaceRotation.D_0;
        this.mirror = PlaceMirror.NONE;
        this.transform = StructureTransform.DEFAULT;
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
    public Vector3i transform(Vector3i relative) {
        return transform.transform(getDestination(), relative.clone(), getPivot(), getMirror(), getRotation());
    }

    public Vec3 transform(Vec3 vec3) {
        return transform.transformDecimal(getDestination(), new Vec3(vec3.x, vec3.y, vec3.z), getPivot(), getMirror(), getRotation());
    }

    public StructureTransform getTransform() {
        return transform;
    }

    public StructurePlaceContext setTransform(StructureTransform transform) {
        this.transform = transform;
        return this;
    }

    public PlaceMirror getMirror() {
        return mirror;
    }

    public StructurePlaceContext setMirror(PlaceMirror mirror) {
        this.mirror = mirror;
        return this;
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

    public StructurePlaceContext addEntityProcessor(EntityProcessor processor) {
        this.entityProcessors.add(processor);
        return this;
    }

    public StructurePlaceContext addEmptyEntityProcessor() {
        return addEntityProcessor(new EmptyEntityProcessor());
    }

    public StructurePlaceContext setPivot(Vector3i pivot) {
        this.pivot = pivot;
        return this;
    }

    public Vector3i getPivot() {
        return pivot.clone();
    }

    public Vector3i getAbsolutePivot() {
        return pivot.clone().add(getDestination());
    }

    public BlockPlacer getPlacer() {
        return placer;
    }

    public StructurePlaceContext placeEntities(boolean placeEntities) {
        this.placeEntities = placeEntities;
        return this;
    }

    public boolean placeEntities() {
        return placeEntities && entityPlacer != null;
    }

    public EntityPlacer getEntityPlacer() {
        return entityPlacer;
    }

    public StructurePlaceContext setEntityPlacer(EntityPlacer entityPlacer) {
        this.entityPlacer = entityPlacer;
        return this;
    }

    EntityPlaceData runProcessors(StructureEntityInfo info) {
        if(entityProcessors.isEmpty()) return null;

        EntityPlaceData data = null;

        for (EntityProcessor p: entityProcessors) {
            data = p.process(info, this, data);
        }

        return data;
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
