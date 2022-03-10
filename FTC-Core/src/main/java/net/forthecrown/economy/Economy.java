package net.forthecrown.economy;

import net.forthecrown.core.Crown;
import net.forthecrown.core.FtcVars;
import net.forthecrown.serializer.CrownSerializer;
import net.minecraft.util.Mth;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Represents the in-game economy.
 * <p></p>
 * Implementation: {@link FtcEconomy}
 */
public interface Economy extends CrownSerializer {

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
    int get(UUID uuid);

    /**
     * Sets the players balance
     * @param uuid The UUID of the player
     * @param amount The new balance of the player
     */
    void set(UUID uuid, int amount);

    /**
     * Adds to a players balance
     * @param uuid The UUID of the player
     * @param amount The amount of Rhines to add to their balance
     */
    void add(UUID uuid, int amount);

    /**
     * Removes an amount for the given UUID
     * @param id the id to remove from
     * @param amount the amount to remove
     */
    void remove(UUID id, int amount);

    /**
     * Gets the % a player is taxed
     * @param uuid The UUID of the player
     * @return the tax bracket of the person
     */
    int getIncomeTax(UUID uuid, int currentBal);

    /**
     * Sets a user's balance, ignoring the maximum balance limit
     * @param id The id of the balance to set
     * @param amount The amount to set the balance
     */
    void setUnlimited(UUID id, int amount);

    /**
     * Checks if the given user can afford losing the given amount
     * @param id The UUID of the user
     * @param amount the amount to check
     * @return Whether they can afford losing that amount
     */
    boolean has(UUID id, int amount);

    /**
     * Checks that the amount for the given UUID is under the max bal limit.
     * <p></p>
     * This returns nothing but it will log a warning if the balance is on or over the bal limit
     * @param uuid The holder of the balance
     * @param amount The amount to check
     */
    static void checkUnderMax(UUID uuid, int amount) {
        if(amount > FtcVars.maxMoneyAmount.get()) {
            Crown.logger().warn(Bukkit.getOfflinePlayer(uuid).getName() + " has reached the balance limit.");
        }
    }

    /**
     * Returns an amount that's within the balance bounds of 0 to {@link FtcVars#maxMoneyAmount}
     * <p>Uses {@link Mth#clamp(long, long, long)}</p>
     * @param amount The amount to clamp
     * @return The amount within the bal limits, or the given amount if the given amount was already in the bounds.
     */
    static int clampToBalBounds(int amount) {
        return Mth.clamp(amount, 0, FtcVars.maxMoneyAmount.get());
    }
}
