package net.forthecrown.utils;

import net.forthecrown.utils.math.AbstractBounds3i;

/** An object which holds an integer bounding box of some sort */
public interface BoundsHolder {
    /**
     * Gets the boundaries of this object
     * @return This object's bounds
     */
    AbstractBounds3i getBounds();
}