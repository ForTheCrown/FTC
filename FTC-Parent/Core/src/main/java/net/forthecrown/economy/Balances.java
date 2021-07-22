package net.forthecrown.economy;

import net.forthecrown.core.chat.ChatFormatter;
import net.forthecrown.serializer.CrownSerializer;
import net.forthecrown.utils.FtcUtils;
import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * Represents the ingame balances held by the players
 */
public interface Balances extends CrownSerializer {

    /**
     * Gets a formatted currency messaage
     * @param amount The amount to format
     * @return The message, will look like: "1,000,000 Rhines"
     */
    static String getFormatted(int amount){
        return ChatFormatter.decimalizeNumber(amount) + " Rhine" + FtcUtils.addAnS(amount);
    }

    /**
     * Same as formatted(int) except not translatable.
     * @param amount The amount to format for
     * @return The formatted message
     */
    static Component formattedNonTrans(int amount) {
        return Component.text(getFormatted(amount));
    }

    /**
     * Same thing as getFormatted but for components, is also translatable
     * @param amount The amount to format
     * @return A formatted, translatable, component
     */
    static Component formatted(int amount){
        return Component.text()
                .content(ChatFormatter.decimalizeNumber(amount) + " ")
                .append(Component.translatable("economy.currency." + (amount == 1 || amount == -1 ? "singular" : "multiple")))
                .build();
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
    void setMap(BalanceMap balanceMap);

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
     * Sets a user's balance, ignoring the maximum balance limit
     * @param id The id of the balance to set
     * @param amount The amount to set the balance
     */
    void setUnlimited(UUID id, Integer amount);

    /**
     * Checks if the given user can afford losing the given amount
     * @param id The UUID of the user
     * @param amount the amount to check
     * @return Whether they can afford losing that amount
     */
    boolean canAfford(UUID id, int amount);
}
