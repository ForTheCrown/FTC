package net.forthecrown.structure;

import net.forthecrown.math.Bounds2i;
import net.forthecrown.math.Rot;
import net.forthecrown.math.Vec2i;

import java.util.List;
import java.util.Map;

public interface StructureNodeType<T extends StructureNode> {
    T create();

    Vec2i entrancePos();

    Bounds2i createBounds(Vec2i pos, Rot rotation);

    Structure structure();
    List<Connector> connectors();
}
