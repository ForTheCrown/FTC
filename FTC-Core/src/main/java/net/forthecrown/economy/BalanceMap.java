package net.forthecrown.economy;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

/**
 * The map object which stores the balances of players.
 * <p></p>
 * Implementation: {@link SortedBalanceMap}
 */
public interface BalanceMap {

    /**
     * Gets the supplier of the default balance amount.
     * <p></p>
     * This is a supplier as the implementation, {@link SortedBalanceMap},
     * uses the startRhines comvar, which could be changed at any time.
     * @return
     */
    IntSupplier getDefaultSupplier();

    /**
     * Gets the default amount returned by the defaultAmount supplier.
     * @return The default balance
     */
    default int getDefaultAmount() {
        return getDefaultSupplier().getAsInt();
    }

    /**
     * Gets whether the given UUID has a balance
     * @param id The ID to check
     * @return Whether the ID has a balance
     */
    boolean contains(UUID id);

    /**
     * Removes a given ID's balance from the map
     * @param id The ID to remove
     */
    void remove(UUID id);

    /**
     * Gets a balance by the given ID
     * @param id The ID to get the balance of
     * @return The given ID's balance, or {@link BalanceMap#getDefaultAmount()} if the given ID doesn't have a balance
     */
    int get(UUID id);

    /**
     * Gets a balance by a list index in the balances list
     * @param index The index to get the balance of
     * @return The balance at the index
     * @throws ArrayIndexOutOfBoundsException If the index is less than 0 or is outside of the list's size
     */
    int get(int index) throws ArrayIndexOutOfBoundsException;

    /**
     * Gets an entry at the index
     * @param index The index to get the entry of
     * @return The entry at the given index
     * @throws ArrayIndexOutOfBoundsException If the index is outside of the balances list
     */
    Balance getEntry(int index) throws ArrayIndexOutOfBoundsException;

    /**
     * Gets an index of a UUID
     * @param id The ID to get the index of
     * @return The index of the id, or -1, if the ID isn't in the map
     */
    int getIndex(UUID id);

    /**
     * Gets the size of the map
     * @return
     */
    int size();

    /**
     * Clears all balances
     */
    void clear();

    /**
     * Sets the given ID's balance to the given amount
     * @param id The ID to set the balance of
     * @param amount The amount to set it to
     */
    void put(UUID id, int amount);

    /**
     * Gets the total balance amount held by this map.
     * @return The list's total balance amount
     */
    long getTotalBalance();

    @Override
    String toString();

    /**
     * A stream of balances meant to be used for serialization
     * <p></p>
     * Warning: NONE OF THE ENTRIES ARE CLONES, do not cast,
     * modify or change the balances in the returned stream
     *
     * @return A stream of all entries in the map,
     */
    Stream<BalanceReader> readerStream();

    interface BalanceReader extends Comparable<BalanceReader> {
        /**
         * Gets the ID of the holder of this balance
         * @return The holder's ID
         */
        UUID getUniqueId();

        /**
         * Gets the balance value held by this entry
         * @return The entry's balance
         */
        int getValue();

        @Override
        default int compareTo(@NotNull BalanceMap.BalanceReader o) {
            return Integer.compare(getValue(), o.getValue());
        }
    }

    /**
     * A single balance entry in the balance map
     * <p></p>
     * Any changes done to the entry's balance alone will not
     * change it's position in the map, rather will just cause
     * disorder in the balance map.
     * <p></p>
     * Do not edit the entry directly unless you have a good
     * reason to do so. Use methods provided in the
     * {@link BalanceMap} to do so, as they will ensure the
     * map stays organized and sorted.
     */
    class Balance implements BalanceReader, Cloneable {
        private final UUID id;
        private int value;

        public Balance(UUID id, int value) {
            this.id = id;
            this.value = value;
        }

        /**
         * Gets the ID of the holder of this balance
         * @return The holder's ID
         */
        @Override
        public UUID getUniqueId() {
            return id;
        }

        /**
         * Gets the balance value held by this entry
         * @return The entry's balance
         */
        @Override
        public int getValue() {
            return value;
        }

        /**
         * Sets the balance held by this entry.
         * <p></p>
         * Note: Does not enforce the map to move this entry to re-sort.
         * Because of that, do not edit entries directly as it could
         * cause balances to become disordered.
         * @param bal The new balance of this entry
         * @return This entry
         */
        public Balance setValue(int bal) {
            this.value = bal;

            //Returns this so the code in SortedBalanceMap.put(UUID, int)
            //is a bit shorter
            return this;
        }

        @Override
        protected Balance clone() {
            return new Balance(getUniqueId(), getValue());
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "id=" + id +
                    ",bal=" + value +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Balance entry = (Balance) o;

            return new EqualsBuilder()
                    .append(id, entry.id)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(id)
                    .toHashCode();
        }
    }
}
