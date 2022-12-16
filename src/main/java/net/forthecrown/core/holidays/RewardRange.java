package net.forthecrown.core.holidays;

import lombok.Data;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

import java.util.Random;

/**
 * A range of rewards for a currency type item
 */
@Data
public class RewardRange {
    public static RewardRange NONE = new RewardRange(0, 0);

    private final int min, max;

    /**
     * Creates a reward range between the given amounts
     * <p>
     * If both of the given values are 0, then {@link #NONE}
     * is returned instead.
     * @param min The minimum amount
     * @param max The maximum amount
     * @return The created range
     */
    public static RewardRange between(int min, int max) {
        if (min == 0 && max == 0) {
            return NONE;
        }

        return new RewardRange(
                Math.min(min, max),
                Math.max(min, max)
        );
    }

    /**
     * Creates a reward range of the exact value
     * <p>
     * If the given value is 0, then {@link #NONE}
     * is returned
     * @param val The value of the range
     * @return The created range
     */
    public static RewardRange exact(int val) {
        if (val == 0) {
            return NONE;
        }

        return new RewardRange(val, val);
    }

    /**
     * Loads the rewards from the given tag
     *
     * @param tag Tag to load from, {@link RewardRange#NONE}, if the tag is null
     * @return The loaded range
     */
    public static RewardRange load(Tag tag) {
        if (tag == null) {
            return NONE;
        }

        if (tag.getId() == Tag.TAG_INT) {
            return exact(((IntTag) tag).getAsInt());
        }

        IntArrayTag arr = (IntArrayTag) tag;
        return between(arr.get(0).getAsInt(), arr.get(1).getAsInt());
    }

    /**
     * Checks if the range is not a range lol
     *
     * @return True if the min == max
     */
    public boolean isExact() {
        return min == max;
    }

    /**
     * Gets if the range represents a NULL value range,
     * meaning a range which gives nothing, it goes from 0 to 0
     *
     * @return True, if this reward will always reward 0
     */
    public boolean isNone() {
        return this == NONE || min > max || (min == 0 && max == 0);
    }

    /**
     * Gets A random, rounded amount this range gives
     * <p>
     * This method will always round down any random
     * initial result it gets, if the initial result that
     * was found is less than 10,000, then it will be
     * rounded down to the nearest 100, otherwise it
     * gets rounded to the nearest 1000.
     *
     * @param random The random to use
     * @return A random amount between min and max, rounded down
     */
    public int get(Random random) {
        if (isNone()) {
            return 0;
        }

        if (isExact()) {
            return min;
        }

        int dif = getSize();
        int initialResult = min + random.nextInt(dif + 1);

        // If less than 10000, we should round down to
        // the next 100 not the next 1000
        int rndValue = initialResult < 10_000 ? 100 : 1000;

        // Round down either by a thousand or a hundred,
        // depending on the initially found random result
        return initialResult - (initialResult % rndValue);
    }

    /**
     * Gets the size of the range
     *
     * @return The difference between the max and min values of this range
     */
    public int getSize() {
        return max - min;
    }

    /**
     * Saves the range
     *
     * @return The saved tag, will be an int tag if this range is exact, otherwise IntArrayTag
     */
    public Tag save() {
        return isExact() ? IntTag.valueOf(min) : new IntArrayTag(new int[]{min, max});
    }

    public String toString() {
        if (isNone()) {
            return "None";
        }

        if (isExact()) {
            return min + "";
        }

        return min + ".." + max;
    }
}