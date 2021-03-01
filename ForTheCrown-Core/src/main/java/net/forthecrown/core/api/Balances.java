package net.forthecrown.core.api;

import java.util.Map;
import java.util.UUID;

public interface Balances extends CrownFileManager {

    Map<UUID, Integer> getBalanceMap();
    void setBalanceMap(Map<UUID, Integer> balanceMap);

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

    /**
     * Returns a more readable version of a person's balance
     * @param id The ID of the balance to get
     * @return A more readable number, example: 1,000,000 instead of 1000000
     */
    String getDecimalizedBalance(UUID id);

    /**
     * Sets a user's balance, ignoring the maximum balance limit
     * @param id The id of the balance to set
     * @param amount The amount to set the balance
     */
    void setLimitlessBalance(UUID id, Integer amount);

}
