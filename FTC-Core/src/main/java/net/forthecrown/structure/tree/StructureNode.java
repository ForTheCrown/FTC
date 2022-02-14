package net.forthecrown.structure.tree;

import net.forthecrown.core.Crown;
import net.forthecrown.structure.PlaceRotation;
import net.forthecrown.utils.math.Vector3i;
import net.forthecrown.utils.transformation.BoundingBoxes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.logging.log4j.Logger;

public abstract class StructureNode {
    private final StructureNodeType type;

    private Vector3i offset;
    protected PlaceRotation rotation;
    protected BoundingBox bounds;

    public StructureNode(StructureNodeType type) {
        this.type = type;
    }

    public StructureNode(StructureNodeType type, CompoundTag tag) {
        this(type);

        if(tag.contains("pos")) this.offset = Vector3i.of(tag.get("pos"));
        if(tag.contains("rot")) this.rotation = PlaceRotation.values()[tag.getByte("rot")];

        if(tag.contains("bounds")) {
            bounds = BoundingBoxes.load(tag.get("bounds"));
        }
    }

    private static final Logger LOGGER = Crown.logger();

    public boolean place(NodePlaceContext context, boolean force) {
        if(!force && context.getDepth() >= getType().getStructureType().maxDepth()) return false;

        if(rotation == null) rotation = PlaceRotation.D_0;
        if(offset == null) offset = Vector3i.ZERO;

        context.addOffset(offset);

        bounds = getType().createBounds(this, context.getEffectivePlacePos(), rotation);

        if(!force && !context.isLegalArea(bounds)) return false;
        context.addGeneratedArea(bounds);

        Crown.logger().info("placePos: {}", context.getEffectivePlacePos().toString());
        return onPlace(context);
    }

    public abstract boolean onPlace(NodePlaceContext context);

    public void save(CompoundTag tag) {
        if(offset != null) tag.put("pos", offset.saveAsTag());
        if(rotation != null) tag.putByte("rot", (byte) rotation.ordinal());

        if(bounds != null) {
            tag.put("bounds", BoundingBoxes.save(bounds));
        }

        saveAdditionalData(tag);
    }

    public abstract void saveAdditionalData(CompoundTag tag);

    public StructureNodeType getType() {
        return type;
    }

    public Vector3i getOffset() {
        return offset;
    }

    public void setOffset(Vector3i offset) {
        Crown.logger().info("setOffset in structure node called, vector: {}", offset);
        this.offset = offset;
    }

    public PlaceRotation getRotation() {
        return rotation == null ? PlaceRotation.D_0 : rotation;
    }

    public void setRotation(PlaceRotation rotation) {
        this.rotation = rotation;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    @Override
    public String toString() {
        return getType().key().asString();
    }
}
