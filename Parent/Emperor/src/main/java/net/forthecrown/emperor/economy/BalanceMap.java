package net.forthecrown.emperor.economy;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface BalanceMap {

    /**
     * Gets the default amount of Rhines a person has
     * @return Default amount of Rhines, normally 100
     */
    int getDefaultAmount();

    /**
     * Checks whether the map contains the UUID
     * @param id The ID to check for
     * @return  Whether the id is in the map
     */
    boolean contains(UUID id);

    /**
     * Removes the id from the map
     * @param id The ID to remove
     */
    void remove(UUID id);

    /**
     * Gets the balance of a player, or the default amount if the ID doesn't have an entry
     * @param id The ID to get the balance of
     * @return The ID's balance
     */
    int get(UUID id);

    /**
     * Gets the balance at a certain map index
     * @param index The index to get the balance of
     * @return The balance at that index
     * @throws ArrayIndexOutOfBoundsException if the index is outside of the map
     */
    int get(int index) throws ArrayIndexOutOfBoundsException;

    /**
     * Gets an entry at an index
     * @param index The index to get entry of
     * @return The entry at the index
     * @throws ArrayIndexOutOfBoundsException if the index is out of bounds
     */
    BalEntry getEntry(int index) throws ArrayIndexOutOfBoundsException;

    /**
     *
     * @param index
     * @return
     * @throws ArrayIndexOutOfBoundsException If the index is out of bounds
     */
    Component getPrettyDisplay(int index) throws ArrayIndexOutOfBoundsException;

    int getIndex(UUID id);

    int size();

    void put(UUID id, int amount);

    long getTotalBalance();

    @Override
    String toString();

    List<UUID> getKeys();

    List<Integer> getValues();

    class BalEntry implements Comparable<BalanceMap.BalEntry> {
        private final UUID id;
        private int bal;

        public BalEntry(UUID id, int bal) {
            this.id = id;
            this.bal = bal;
        }

        public UUID getUniqueId() {
            return id;
        }

        public int getValue() {
            return bal;
        }

        public void setValue(int bal) {
            this.bal = bal;
        }

        @Override
        public int compareTo(@NotNull BalanceMap.BalEntry o) {
            return Integer.compare(bal, o.bal);
        }
    }
}
