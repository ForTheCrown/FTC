package net.forthecrown.core.animation;

import net.forthecrown.core.Crown;
import net.forthecrown.utils.transformation.BoundingBoxes;
import net.forthecrown.utils.math.Vector3i;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A simple animation that plays by editing blocks in an area from an
 * array of pre-made frames at a given rate
 * <p></p>
 * No getter exists for frames as that would make them mutable, no good
 * <p></p>
 * onSuccessEnd can also be used to chain together animations, such as
 * for a mob made of blocks. Taking it from a say attack state to a calm
 * state. Or to allow a dancing stick figure to transition from one
 * animation to another.
 * <p></p>
 * To make a block animation see: {@link AnimationBuilder}
 *
 * @see AnimationBuilder
 */
public class BlockAnimation implements Keyed {
    //Immutable variables
    private final Key key;

    private final int ticksPerFrame;
    private final BlockAnimationFrame[] frames;

    private final AnimationConsumer onPlay;
    private final AnimationConsumer onSuccessEnd;

    //Changing variables
    private boolean playing;
    private int currentFrame;
    private int currentID;

    public BlockAnimation(Key key, int ticksPerFrame, BlockAnimationFrame[] frames, AnimationConsumer onPlay, AnimationConsumer onSuccessEnd) {
        this.key = key;
        this.ticksPerFrame = ticksPerFrame;
        this.frames = frames;
        this.onPlay = onPlay;
        this.onSuccessEnd = onSuccessEnd;
    }

    /**
     * Gets the tick interval between frames
     * @return The frame tick interval
     */
    public int getTicksPerFrames() {
        return ticksPerFrame;
    }

    /**
     * Gets whether the animation is currently playing
     * @return Whether the animations is currently playing
     */
    public boolean isPlaying() {
        return playing;
    }

    /**
     * Gets the index of the current frame
     * @return The current frame index
     */
    public int getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Plays the animation at the given location
     * @see BlockAnimation#play(World, Vector3i)
     * @param loc The location to play at
     */
    public void play(Location loc) {
        play(loc.getWorld(), Vector3i.of(loc));
    }

    /**
     * Plays the animation in the given world and at the given position
     * <p>Warning: Will throw an exception if the animation is already playing</p>
     * @param world The world to play in
     * @param pos The position to play at
     */
    public void play(World world, Vector3i pos) {
        Validate.isTrue(!playing, "Animation is already playing");

        if(onPlay != null) onPlay.run(world, pos);

        playing = true;
        currentFrame = 0;

        currentID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Crown.inst(), () -> {
            BlockAnimationFrame frame = frames[currentFrame];

            //If somehow the frame is null, else do the animation
            //It must always move on to the next frame or to the end of the animation
            if(frame == null) {
                Crown.logger().warn("Found null frame at index " + currentFrame);
                Crown.logger().warn(Arrays.toString(frames));
            } else  {
                //Apply offset
                Vector3i at = frame.getOffset().apply(pos);

                //Paste the frame
                BoundingBoxes.copyTo(world, frame.getFrameOrigin(), at.toWorldVector(world));

                //Run onPlay function
                if(frame.getOnPlay() != null) {
                    frame.getOnPlay().run(world, at);
                }
            }

            currentFrame++;

            //If we've reached the end of the animation
            //Stop animation
            if(currentFrame >= frames.length) {
                stopPlaying();
                if(onSuccessEnd != null) onSuccessEnd.run(world, pos);
            }

        }, getTicksPerFrames(), getTicksPerFrames());
    }

    /**
     * Stops playing the animation.
     * <p>Warning: Will throw an exception if the animation is not currently playing</p>
     */
    public void stopPlaying() {
        Validate.isTrue(playing, "Animation is not playing");

        Bukkit.getScheduler().cancelTask(currentID);
        playing = false;
        currentID = 0;
        currentFrame = 0;
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
