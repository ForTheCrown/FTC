package net.forthecrown.structure;

import net.forthecrown.math.Bounds2i;
import net.forthecrown.math.Rot;
import net.forthecrown.math.Vec2i;

public abstract class StructureNode {
    private final StructureNodeType type;

    private Vec2i offset = Vec2i.ZERO;
    private Vec2i pivot = Vec2i.ZERO;
    private Rot rotation = Rot.D_0;

    public StructureNode(StructureNodeType type) {
        this.type = type;
    }

    public StructureNodeType getType() {
        return type;
    }

    public boolean place(PlaceContext context, boolean force) {
        if(!force && context.getDepth() >= getType().structure().maxDepth()) return false;

        if(rotation == null) rotation = Rot.D_0;
        if(offset == null) offset = Vec2i.ZERO;

        context.addOffset(offset);
        Bounds2i bounds = getType().createBounds(context.getEffectivePlace(), rotation);

        if(!force && !context.legalArea(bounds)) return false;
        context.addArea(bounds);

        return onPlace(context);
    }

    public abstract boolean onPlace(PlaceContext context);

    public Rot getRotation() {
        return rotation;
    }

    public void setRotation(Rot rotation) {
        this.rotation = rotation;
    }

    public void setOffset(Vec2i offset) {
        this.offset = offset;
    }

    public Vec2i getOffset() {
        return offset;
    }

    public Vec2i getPivot() {
        return pivot;
    }

    public void setPivot(Vec2i pivot) {
        this.pivot = pivot;
    }
}
