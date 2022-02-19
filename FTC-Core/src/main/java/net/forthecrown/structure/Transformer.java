package net.forthecrown.structure;

import net.forthecrown.utils.math.Vector3i;

public interface Transformer {
    Vector3i transform(Vector3i pos);
}
