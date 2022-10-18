package net.forthecrown.useables;

import net.forthecrown.utils.math.WorldBounds3i;
import org.spongepowered.math.vector.Vector3i;

/**
 * A {@link UsableTrigger}'s type.
 * <p>
 * This determines when the trigger is activated based
 * on if the player is inside the trigger area, whether
 * they're leaving or entering it or either.
 */
public enum TriggerType {
    /** Type which allows activation if the player has entered the trigger */
    ENTER {
        @Override
        public boolean shouldRun(WorldBounds3i bounds3i, Vector3i pos, Vector3i dest) {
            return bounds3i.contains(dest) && !bounds3i.contains(pos);
        }
    },

    /** Allows activation when the player is leaving the trigger */
    EXIT {
        @Override
        public boolean shouldRun(WorldBounds3i bounds3i, Vector3i pos, Vector3i dest) {
            return !bounds3i.contains(dest) && bounds3i.contains(pos);
        }
    },

    /** Activates either when the player leaves or enters */
    EITHER {
        @Override
        public boolean shouldRun(WorldBounds3i bounds3i, Vector3i pos, Vector3i dest) {
            return EXIT.shouldRun(bounds3i, pos, dest) || ENTER.shouldRun(bounds3i, pos, dest);
        }
    },

    /** Activates whenever a player moves inside the trigger */
    MOVE {
        @Override
        public boolean shouldRun(WorldBounds3i bounds3i, Vector3i pos, Vector3i dest) {
            return bounds3i.contains(dest);
        }
    };

    public abstract boolean shouldRun(WorldBounds3i bounds3i, Vector3i pos, Vector3i dest);
}