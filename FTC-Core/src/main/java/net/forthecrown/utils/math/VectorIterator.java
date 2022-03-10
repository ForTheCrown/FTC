package net.forthecrown.utils.math;

import java.util.NoSuchElementException;

public class VectorIterator<T extends AbstractVector3i<T>> extends AbstractPosIterator<T, T> {
    public VectorIterator(T min, T max, long maxIteration) {
        super(min, max, maxIteration);
    }

    @Override
    public T next() {
        if(!hasNext()) throw new NoSuchElementException();
        advance();

        return min.cloneAt(x, y, z, true);
    }
}
