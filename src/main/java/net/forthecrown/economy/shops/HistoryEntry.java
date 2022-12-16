package net.forthecrown.economy.shops;

import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.Validate;

import java.util.UUID;

/**
 * A single entry in a shop's use history, more-or-less represents a single
 * instance of a {@link SignShopSession}'s interaction with a shop.
 * <p>
 * This class is serialized in a <i>more compact</i> way than normal into a long
 * array using {@link #toArray()}
 * @see #toArray() for how entries are serialized
 */
public record HistoryEntry(long date, UUID customer, int amount, int earned, boolean wasBuy) {
    /* ----------------------------- CONSTANTS ------------------------------ */
    /** Index of the date time stamp in the {@link #toArray()} result. */
    private static final int INDEX_DATE     = 0;

    /**
     * Index of the most significant bits of customer's UUID
     * in the {@link #toArray()} result.
     */
    private static final int INDEX_ID_MOST  = 1;

    /**
     * Index of the least significant bits of customer's UUID
     * in the {@link #toArray()} result,
     */
    private static final int INDEX_ID_LEAST = 2;

    /** Index of the traded item amount in the {@link #toArray()} result. */
    private static final int INDEX_AMOUNT   = 3;

    /** Index of the earned amount of rhines in the {@link #toArray()} result. */
    private static final int INDEX_EARNED   = 4;

    /** Index of the transaction type, either 0 or 1, in the {@link #toArray()} result. */
    private static final int INDEX_TYPE     = 5;

    /** The length of the array returned in {@link #toArray()} */
    private static final int DATA_LENGTH = INDEX_TYPE + 1;

    /* ----------------------------- SERIALIZATION ------------------------------ */

    /**
     * Loads a history entry from the given NBT tag.
     * <p>
     * Just calls {@link #of(long[])} by casting the
     * given tag to a {@link LongArrayTag}
     *
     * @param t The NBT tag to load from
     * @return The loaded entry
     */
    public static HistoryEntry of(Tag t) {
        return of(((LongArrayTag) t).getAsLongArray());
    }

    /**
     * Loads the history entry from the given long
     * array.
     * @see #toArray()
     * @param data The data to load from
     * @return The loaded entry
     */
    public static HistoryEntry of(long[] data) {
        Validate.isTrue(data.length != DATA_LENGTH, "Invalid data size");

        return new HistoryEntry(
                data[INDEX_DATE],
                new UUID(data[INDEX_ID_MOST], data[INDEX_ID_LEAST]),
                (int) data[INDEX_AMOUNT],
                (int) data[INDEX_EARNED],
                data[INDEX_TYPE] == 1
        );
    }

    /**
     * Serializes this entry into a long array
     * <p>
     * Format:
     * I'll briefly explain the format for this serialization
     * is: <pre>
     * Index 0: the timestamp the entry was created
     * Index 1, 2: The most and least significant
     *             bits of the customer's UUID
     * Index 3: The amount of items bought
     * Index 4: The amount of money earned
     * Index 5: The type of transaction, 1 for buy,
     *          0 for sell
     * </pre>
     * Each index has its own constant as well
     * @return A long array of data representing this entry
     * @see #DATA_LENGTH
     * @see #INDEX_DATE
     * @see #INDEX_ID_MOST
     * @see #INDEX_ID_LEAST
     * @see #INDEX_AMOUNT
     * @see #INDEX_EARNED
     * @see #INDEX_TYPE
     */
    public long[] toArray() {
        long[] result = new long[DATA_LENGTH];

        result[INDEX_DATE] = date;

        result[INDEX_ID_MOST] = customer.getMostSignificantBits();
        result[INDEX_ID_LEAST] = customer.getLeastSignificantBits();

        result[INDEX_AMOUNT] = amount;
        result[INDEX_EARNED] = earned;
        result[INDEX_TYPE] = wasBuy ? 1 : 0;

        return result;
    }

    /**
     * Saves this entry into an NBT long array tag
     * using the array returned by {@link #toArray()}
     * @return The saved NBT tag
     */
    public LongArrayTag save() {
        return new LongArrayTag(toArray());
    }
}