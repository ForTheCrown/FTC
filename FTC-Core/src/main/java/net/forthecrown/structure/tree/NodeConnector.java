package net.forthecrown.structure.tree;

import net.forthecrown.structure.PlaceMirror;
import net.forthecrown.structure.PlaceRotation;
import net.forthecrown.structure.StructureTransform;
import net.forthecrown.utils.math.Vector3i;

public record NodeConnector(Vector3i offset, PlaceRotation rotation) {
    public static final NodeConnector EMPTY = new NodeConnector(Vector3i.ZERO, PlaceRotation.D_0);

    public void apply(StructureNode node) {
        node.setOffset(offset);
        node.setRotation(rotation);
    }

    public Vector3i offset(PlaceRotation rotation) {
        return StructureTransform.DEFAULT.transform(Vector3i.ZERO, offset, Vector3i.ZERO, PlaceMirror.NONE, rotation);
    }
}
