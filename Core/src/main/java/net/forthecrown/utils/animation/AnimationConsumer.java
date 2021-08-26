package net.forthecrown.utils.animation;

import net.forthecrown.utils.math.Vector3i;
import org.bukkit.World;

/**
 * A function which takes in a world and a position.
 */
@FunctionalInterface
public interface AnimationConsumer {
    /**
     * Function that'll perform some code during, before or after a block animation
     * @param world The world the animation is happening in
     * @param pos The position the animation happens at
     */
    void run(World world, Vector3i pos);
}
