package net.forthecrown.structure;

import lombok.experimental.UtilityClass;
import net.forthecrown.utils.VanillaAccess;
import net.forthecrown.utils.math.Transform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for methods and constants related
 * to {@link BlockProcessor}s
 */
public @UtilityClass class BlockProcessors {
    /* ----------------------------- CONSTANTS ------------------------------ */

    /**
     * A block processor which will either return the original block's
     * data or the data of the previous processor, depending on if the
     * previous result was null or not
     */
    public final BlockProcessor NON_NULL_PROCESSOR = new NonNullProcessor();

    /**
     * A processor which rotates every given block, according to a
     * given {@link StructurePlaceConfig}'s rotation value
     */
    public final BlockProcessor ROTATION_PROCESSOR = new RotationProcessor();

    /** Block processor which will ignore all air blocks and not place them */
    public final BlockProcessor IGNORE_AIR = new IgnoreAirProcessor();

    /* ----------------------------- SUB CLASSES ------------------------------ */

    private class NonNullProcessor implements BlockProcessor {
        @Override
        public @Nullable BlockInfo process(@NotNull  BlockInfo original,
                                           @Nullable BlockInfo previous,
                                           @NotNull  StructurePlaceConfig context
        ) {
            return previous == null ? original : previous;
        }
    }

    private class IgnoreAirProcessor implements BlockProcessor {
        @Override
        public @Nullable BlockInfo process(@NotNull  BlockInfo original,
                                           @Nullable BlockInfo previous,
                                           @NotNull  StructurePlaceConfig config
        ) {
            if (previous == null) {
                return null;
            }

            if (previous.getData().getMaterial().isAir()) {
                return null;
            }

            return previous;
        }
    }

    private class RotationProcessor implements BlockProcessor {
        @Override
        public @Nullable BlockInfo process(@NotNull  BlockInfo original,
                                           @Nullable BlockInfo previous,
                                           @NotNull  StructurePlaceConfig context
        ) {
            if (previous == null
                    || context.getTransform() == null
                    || context.getTransform().isIdentity()
            ) {
                return previous;
            }
            Transform transform = context.getTransform();

            // So fun fact, rotation is built into vanilla, Bukkit,
            // in their infinite wisdom haven't made that part API
            // though, the geniuses that they are
            var data = VanillaAccess.rotate(previous.getData(), transform.getRotation());
            return previous.withData(data);
        }
    }


}