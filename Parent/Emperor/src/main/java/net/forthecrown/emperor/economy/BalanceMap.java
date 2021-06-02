package net.forthecrown.emperor.economy;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface BalanceMap {

    int getDefaultAmount();

    boolean contains(UUID id);

    void remove(UUID id);

    int get(UUID id);

    int get(int index) throws ArrayIndexOutOfBoundsException;

    BalEntry getEntry(int index) throws ArrayIndexOutOfBoundsException;

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
