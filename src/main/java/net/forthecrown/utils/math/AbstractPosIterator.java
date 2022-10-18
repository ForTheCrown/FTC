package net.forthecrown.utils.math;

import lombok.Getter;
import org.spongepowered.math.vector.Vector3i;

import java.util.Iterator;

@Getter
public abstract class AbstractPosIterator<E> implements Iterator<E> {
    protected final Vector3i min;
    protected final Vector3i max;
    protected final long maxIteration;

    protected long iteration;
    protected int x, y, z;

    public AbstractPosIterator(Vector3i min, Vector3i max, long maxIteration) {
        this.min = min;
        this.max = max;
        this.maxIteration = maxIteration;

        x = min.x();
        y = min.y();
        z = min.z();
    }

    @Override
    public boolean hasNext() {
        return iteration < maxIteration;
    }

    protected void advance() {
        iteration++;
        x++;

        if(x > max.x()) {
            x = min.x();
            y++;

            if(y > max.y()) {
                y = min.y();
                z++;

                if(z > max.z()) {
                    z = min.z();
                }
            }
        }
    }
}