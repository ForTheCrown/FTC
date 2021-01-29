package net.forthecrown.core.api;

import java.util.UUID;

public interface CrownBalances {

    /**
     * Gets the balance of a player by their UUID
     * @param uuid the UUID of the player
     * @return The balance of the player
     */
    Integer getBalance(UUID uuid);

    /**
     * Sets the players balance
     * @param uuid The UUID of the player
     * @param amount The new balance of the player
     */
    void setBalance(UUID uuid, Integer amount);

    /**
     * Adds to a players balance
     * @param uuid The UUID of the player
     * @param amount The amount of Rhines to add to their balance
     */
    void addBalance(UUID uuid, Integer amount);

    /**
     * Adds to a players balance
     * @param uuid The UUID of the player
     * @param amount The amount to add
     * @param isTaxed Whether the transaction is taxed, aka, if a % doesn't reach the player
     */
    void addBalance(UUID uuid, Integer amount, boolean isTaxed);

    /**
     * Gets the % a player is taxed
     * @param uuid The UUID of the player
     * @return the tax bracket of the person
     */
    Integer getTaxPercentage(UUID uuid);

}
