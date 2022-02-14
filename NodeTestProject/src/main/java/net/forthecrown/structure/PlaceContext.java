package net.forthecrown.structure;

import net.forthecrown.DrawThing;
import net.forthecrown.math.Bounds2i;
import net.forthecrown.math.Transform;
import net.forthecrown.math.Vec2i;

import java.util.ArrayList;
import java.util.List;

public class PlaceContext {
    private static final List<Bounds2i> GENERATED = new ArrayList<>();
    public static DrawThing drawThing;

    private final Vec2i placePos = Vec2i.ZERO;
    private final Transform transform = Transform.DEFAULT;
    private int depth;
    private Vec2i offset;

    public Vec2i getPlacePos() {
        return placePos;
    }

    public Transform getTransform() {
        return transform;
    }

    public int getDepth() {
        return depth;
    }

    public Vec2i getEffectivePlace() {
        return placePos.add(offset);
    }

    public Vec2i getOffset() {
        return offset == null ? Vec2i.ZERO : offset;
    }

    public void addOffset(Vec2i off) {
        offset = getOffset().add(off);
    }

    public boolean legalArea(Bounds2i area) {
        for (Bounds2i i: GENERATED) {
            if(i.overlaps(area)) return false;
        }

        return true;
    }

    public void addArea(Bounds2i area) {
        GENERATED.add(area);
    }

    public DrawThing getDrawThing() {
        return drawThing;
    }

    public PlaceContext copy() {
        PlaceContext context = new PlaceContext();
        context.depth = depth + 1;
        context.offset = offset;

        return context;
    }
}
