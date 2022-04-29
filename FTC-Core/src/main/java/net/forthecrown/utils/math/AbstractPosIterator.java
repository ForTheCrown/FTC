package net.forthecrown.utils.math;

import java.util.Iterator;

public abstract class AbstractPosIterator<T extends AbstractVector3i<T>, E> implements Iterator<E> {
    protected final T min;
    protected final T max;
    protected final long maxIteration;

    protected long iteration;
    protected int x, y, z;

    public AbstractPosIterator(T min, T max, long maxIteration) {
        this.min = min;
        this.max = max;
        this.maxIteration = maxIteration;

        x = min.getX();
        y = min.getY();
        z = min.getZ();
    }

    public T getMax() {
        return max;
    }

    public T getMin() {
        return min;
    }

    public long getMaxIteration() {
        return maxIteration;
    }

    public long getIteration() {
        return iteration;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean hasNext() {
        return iteration < maxIteration;
    }

    protected void advance() {
        iteration++;
        x++;

        if(x > max.getX()) {
            x = min.getX();
            y++;

            if(y > max.getY()) {
                y = min.getY();
                z++;

                if(z > max.getZ()) {
                    z = min.getZ();
                }
            }
        }
    }
}