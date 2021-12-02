package net.forthecrown.economy;

public interface BalanceHolder {
    int getBalance();
    void setBalance(int balance);

    default void addBalance(int amount) {
        setBalance(getBalance() + amount);
    }

    default void removeBalance(int amount) {
        setBalance(getBalance() - amount);
    }

    default boolean hasBalance(int amount) {
        return getBalance() >= amount;
    }
}
