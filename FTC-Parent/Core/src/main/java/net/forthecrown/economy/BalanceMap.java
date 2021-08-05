package net.forthecrown.economy;

import net.kyori.adventure.text.Component;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.IntSupplier;

/**
 * The map object which stores the balances of player.
 * <p></p>
 * Implementation: {@link SortedBalanceMap}
 */
public interface BalanceMap {

    IntSupplier getDefaultSupplier();

    default int getDefaultAmount() {
        return getDefaultSupplier().getAsInt();
    }

    boolean contains(UUID id);

    void remove(UUID id);

    int get(UUID id);

    int get(int index) throws ArrayIndexOutOfBoundsException;

    BalEntry getEntry(int index) throws ArrayIndexOutOfBoundsException;

    Component getPrettyDisplay(int index) throws ArrayIndexOutOfBoundsException;

    int getIndex(UUID id);

    int size();

    void clear();

    void put(UUID id, int amount);

    long getTotalBalance();

    @Override
    String toString();

    List<UUID> keySet();

    List<Integer> values();

    List<BalEntry> entries();

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

        @Override
        public String toString() {
            return getClass().getSimpleName() + "{" +
                    "id=" + id +
                    ",bal=" + bal +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            BalEntry entry = (BalEntry) o;

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
