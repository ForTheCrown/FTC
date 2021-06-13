package net.forthecrown.core.economy;

import net.forthecrown.core.CrownCore;
import net.forthecrown.core.serializer.CrownSerializer;
import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.core.utils.CrownUtils;
import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * Represents the ingame balances held by the players
 */
public interface Balances extends CrownSerializer<CrownCore> {

    /**
     * Gets the Balanaces instance
     * @return The Balances instance
     */
    static Balances inst(){
        return CrownCore.getBalances();
    }

    /**
     * Gets a formatted currency messaage
     * @param amount The amount to format
     * @return The message, will look like: "1,000,000 Rhines"
     */
    static String getFormatted(int amount){
        return ChatFormatter.decimalizeNumber(amount) + " Rhine" + CrownUtils.addAnS(amount);
    }

    /**
     * Same thing as getFormatted but for components
     * @param amount
     * @return
     */
    static Component formatted(int amount){
        if (amount == 1 || amount == -1){
            return Component.text(ChatFormatter.decimalizeNumber(amount) + " ").append(Component.translatable("economy.currency.singular"));
        }

        return Component.text(ChatFormatter.decimalizeNumber(amount) + " ").append(Component.translatable("economy.currency.multiple"));
    }

    /**
     * Gets the map of all balances on the server
     * @return Every person's balance
     */
    BalanceMap getMap();

    /**
     * Sets the balance map
     * @param balanceMap The balance map
     */
    void setMap(SortedBalanceMap balanceMap);

    /**
     * Gets the balance of a player by their UUID
     * @param uuid the UUID of the player
     * @return The balance of the player
     */
    Integer get(UUID uuid);

    /**
     * Sets the players balance
     * @param uuid The UUID of the player
     * @param amount The new balance of the player
     */
    void set(UUID uuid, Integer amount);

    Component withCurrency(UUID id);

    /**
     * Adds to a players balance
     * @param uuid The UUID of the player
     * @param amount The amount of Rhines to add to their balance
     */
    void add(UUID uuid, Integer amount);

    /**
     * Adds to a players balance
     * @param uuid The UUID of the player
     * @param amount The amount to add
     * @param isTaxed Whether the transaction is taxed, aka, if a % doesn't reach the player
     */
    void add(UUID uuid, Integer amount, boolean isTaxed);

    /**
     * Gets the % a player is taxed
     * @param uuid The UUID of the player
     * @return the tax bracket of the person
     */
    Integer getTax(UUID uuid);

    /**
     * Returns a more readable version of a person's balance
     * @param id The ID of the balance to get
     * @return A more readable number, example: 1,000,000 instead of 1000000
     */
    String getDecimalized(UUID id);

    /**
     * Sets a user's balance, ignoring the maximum balance limit
     * @param id The id of the balance to set
     * @param amount The amount to set the balance
     */
    void setUnlimited(UUID id, Integer amount);

    /**
     * Gets a balance string as: "AMOUNT Rhines" or "AMOUNT Rhine" if the amount is 1
     * @param id The ID of the balance to get
     * @return The balance message
     */
    String getWithCurrency(UUID id);

    /**
     * Checks if the given user can afford losing the given amount
     * @param id The UUID of the user
     * @param amount the amount to check
     * @return Whether they can afford losing that amount
     */
    boolean canAfford(UUID id, int amount);
}
