package net.forthecrown.core.animation;

import com.google.common.base.MoreObjects;
import net.forthecrown.utils.Struct;
import net.forthecrown.utils.math.Vector3iOffset;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents a single frame of block animation
 */
public class BlockAnimationFrame implements Struct {
    private final AnimationConsumer onPlay;
    private final BoundingBox frameOrigin;
    private final Vector3iOffset posOffset;

    public BlockAnimationFrame(AnimationConsumer onPlay, BoundingBox frameOrigin, Vector3iOffset posOffset) {
        this.onPlay = onPlay;
        this.frameOrigin = Validate.notNull(frameOrigin);
        this.posOffset = posOffset == null ? Vector3iOffset.ZERO : posOffset;
    }

    public BlockAnimationFrame(BoundingBox frameOrigin, Vector3iOffset posOffset) {
        this(null, frameOrigin, posOffset);
    }

    public BlockAnimationFrame(AnimationConsumer onPlay, BoundingBox frameOrigin) {
        this(onPlay, frameOrigin, null);
    }

    public BlockAnimationFrame(BoundingBox frameOrigin) {
        this(null, frameOrigin, null);
    }

    public BlockAnimationFrame(int mX, int mY, int mZ, int maxX, int maxY, int maxZ) {
        this(new BoundingBox(mX, mY, mZ, maxX, maxY, maxZ));
    }

    /**
     * Gets the offset that this frame has from placement location
     * @return Position offset
     */
    public Vector3iOffset getOffset() {
        return posOffset;
    }

    /**
     * Gets the origin of the frame, aka, the place where the copy blocks for the frame are.
     * @return The place where the frame's blocks are
     */
    public BoundingBox getFrameOrigin() {
        return frameOrigin;
    }

    /**
     * Gets the code to run when this frame is played
     * @return The code to run when this frame is ran, or null, if there's nothing to run.
     */
    public AnimationConsumer getOnPlay() {
        return onPlay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BlockAnimationFrame frame = (BlockAnimationFrame) o;

        return new EqualsBuilder()
                .append(getOnPlay(), frame.getOnPlay())
                .append(getFrameOrigin(), frame.getFrameOrigin())
                .append(getOffset(), frame.getOffset())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getOnPlay())
                .append(getFrameOrigin())
                .append(getOffset())
                .toHashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("posOffset", posOffset)
                .add("frameOrigin", frameOrigin)
                .omitNullValues()
                .toString();
    }
}
