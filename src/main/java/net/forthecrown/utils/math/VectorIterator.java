package net.forthecrown.utils.math;

import org.spongepowered.math.vector.Vector3i;

import java.util.NoSuchElementException;

public class VectorIterator extends AbstractPosIterator<Vector3i> {
    public VectorIterator(Vector3i min, Vector3i max, long maxIteration) {
        super(min, max, maxIteration);
    }

    @Override
    public Vector3i next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        advance();
        return Vector3i.from(x, y, z);
    }
}