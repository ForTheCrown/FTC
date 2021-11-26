package net.forthecrown.core.animation;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.forthecrown.registry.Registries;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.key.Key;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;

import java.util.Arrays;

/**
 * Class which builds block animations
 * @see BlockAnimation
 */
public class AnimationBuilder {
    private final Key key;

    private int ticksPerFrame = 1;
    private ObjectList<BlockAnimationFrame> frames = new ObjectArrayList<>();

    private AnimationConsumer onPlay;
    private AnimationConsumer onSuccessEnd;

    public AnimationBuilder(Key key) {
        this.key = key;
    }

    public AnimationBuilder(String strKey) {
        this.key = FtcUtils.parseKey(strKey);
    }

    /**
     * Sets the amount of ticks between each frame of animation, default is 1
     * @param ticksPerFrame ticks between each frame
     * @return This builder
     */
    public AnimationBuilder setTicksPerFrame(int ticksPerFrame) {
        this.ticksPerFrame = ticksPerFrame;
        return this;
    }

    /**
     * Adds a frame to the animation
     * @param frame The frame to add
     * @return This builder
     */
    public AnimationBuilder addFrame(BlockAnimationFrame frame) {
        this.frames.add(frame);
        return this;
    }

    /**
     * Adds all the given bounding boxes as simple frames
     * @param frames The frames to add
     * @return This builder
     */
    public AnimationBuilder addFrames(BoundingBox... frames) {
        Validate.notNull(frames, "Frames are null");

        for (BoundingBox b: frames) {
            addFrame(new BlockAnimationFrame(b));
        }

        return this;
    }

    /**
     * Adds all the given frames into the animation
     * @param frames The frames to add
     * @return This builder
     */
    public AnimationBuilder addFrames(BlockAnimationFrame... frames) {
        Validate.notNull(frames, "Frames are null");
        this.frames.addAll(Arrays.asList(frames));
        return this;
    }

    /**
     * Sets the code that will be ran when the animations begins to play
     * @param consumer Action to run on animation play
     * @return This builder
     */
    public AnimationBuilder onPlay(AnimationConsumer consumer) {
        this.onPlay = consumer;
        return this;
    }

    /**
     * Sets the code that will be ran when the animation successfully ends.
     * <p>Will not be called when the animation is cancelled.</p>
     * @param consumer Action to run on successfull animation end
     * @return This builder
     */
    public AnimationBuilder onSuccessEnd(AnimationConsumer consumer) {
        this.onSuccessEnd = consumer;
        return this;
    }

    /**
     * Builds the animation with the current settings
     * @return The built animation
     */
    public BlockAnimation build() {
        return new BlockAnimation(
                key,
                ticksPerFrame,
                frames.toArray(BlockAnimationFrame[]::new),
                onPlay, onSuccessEnd
        );
    }

    /**
     * Builds the animation and registers it into the animations registry
     * @return The built and registered animation
     */
    public BlockAnimation buildAndRegister() {
        return Registries.ANIMATIONS.register(key, build());
    }
}
