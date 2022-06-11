package net.forthecrown.structure;

import net.forthecrown.utils.math.Vector3i;

/**
 * A functional interface that transforms a
 * given {@link Vector3i} in some way.
 */
@FunctionalInterface
public interface Transformer {
    /**
     * Transforms the given position
     * @param pos The given position
     * @return The transformed position
     */
    Vector3i transform(Vector3i pos);
}